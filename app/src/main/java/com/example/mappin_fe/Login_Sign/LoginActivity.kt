package com.example.mappin_fe.Login_Sign // 패키지 이름을 소문자로 변경

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mappin_fe.R
import com.example.mappin_fe.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.common.SignInButton

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnGoogleSignIn: SignInButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_pwd)
        val btnLogin: Button = findViewById(R.id.btn_login)
        val btnRegister: Button = findViewById(R.id.btn_register)
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in)

        // Google Sign-In 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Activity Result API 설정
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.result
                    if (account != null) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        firebaseAuth.signInWithCredential(credential)
                            .addOnCompleteListener(this) { authTask ->
                                if (authTask.isSuccessful) {
                                    checkUserSettings()
                                } else {
                                    Toast.makeText(this, "Google Sign-In Failed: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Google Sign-In Failed: No Account", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 이메일/비밀번호 로그인 검증
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            checkUserSettings()
                        } else {
                            Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun checkUserSettings() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

            database.get().addOnSuccessListener { dataSnapshot ->
                val userAccount = dataSnapshot.getValue(UserAccount::class.java)

                if (userAccount != null && userAccount.nickname != null && userAccount.interests != null) {
                    // 사용자 정보가 완전하면 MainActivity로 이동
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // 사용자 정보가 불완전하면 UserSettingsActivity로 이동
                    val intent = Intent(this, UserSettingsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to check user settings", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
