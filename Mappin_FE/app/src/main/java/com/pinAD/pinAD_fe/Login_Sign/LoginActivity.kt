package com.pinAD.pinAD_fe.Login_Sign

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.pinAD.pinAD_fe.Data.login_register.LoginRequest
import com.pinAD.pinAD_fe.Data.login_register.RegisteredAccount
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.common.SignInButton
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.navercorp.nid.oauth.view.NidOAuthLoginButton
import com.pinAD.pinAD_fe.BaseActivity
import com.pinAD.pinAD_fe.user_setting.UserSettingsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class LoginActivity : BaseActivity() {

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
            .requestServerAuthCode(getString(R.string.default_web_client_id))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Activity Result API 설정
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.result
                    val authCode = account.serverAuthCode
                    val idToken = account.idToken
                    Log.d("idtoken", "$idToken")
                    Log.d("AuthCode", "Auth Code: $authCode")
                    sendIdTokenToServer(account.idToken!!)
                } catch (e: Exception) {
                    Toast.makeText(this, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
                login(email, password)
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveTokens(accessToken: String?, refreshToken: String?) {
        val preferences = getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE)
        preferences.edit().apply {
            putString("ACCESS_TOKEN", accessToken)
            putString("REFRESH_TOKEN", refreshToken)
            apply()
        }
    }

    private fun login(email: String, password: String) {
        // FCM 토큰 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcm_token = task.result // 비동기 호출로 토큰 가져오기

                // CoroutineScope를 사용하여 비동기 로그인 처리
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitInstance.api.loginown(RegisteredAccount(email, password, fcm_token!!)) // FCM 토큰 포함
                        withContext(Dispatchers.Main) {
                            when (response.code()) {
                                200 -> {
                                    // 로그인 성공
                                    val loginResponse = response.body()
                                    saveTokens(loginResponse?.access_token, loginResponse?.refresh_token)
                                    RetrofitInstance.setTokens(loginResponse?.access_token, loginResponse?.refresh_token)
                                    Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                                    navigateToNextScreen(loginResponse?.access_token)
                                }
                                400 -> {
                                    // 로그인 실패
                                    Toast.makeText(this@LoginActivity, "로그인 실패: 잘못된 이메일 또는 비밀번호", Toast.LENGTH_SHORT).show()
                                }
                                403 -> {
                                    // 계정 비활성화
                                    Toast.makeText(this@LoginActivity, "계정이 비활성화 상태입니다", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    // 기타 에러 처리
                                    Toast.makeText(this@LoginActivity, "오류 발생: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this@LoginActivity, "FCM 토큰을 가져오는 데 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // 로그인 시도 전 확인
    private fun signInWithGoogle() {
        try {
            Log.d("GoogleSignIn", "Starting Google Sign In")
            val signInIntent = googleSignInClient.signInIntent
            Log.d("GoogleSignIn", "Created sign in intent")
            googleSignInLauncher.launch(signInIntent)
            Log.d("GoogleSignIn", "Launched sign in intent")
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error in signInWithGoogle", e)
        }
    }

    private fun sendIdTokenToServer(idToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // FCM 토큰 가져오기
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                Log.d("fcmToken", "$fcmToken")
                val loginRequest = LoginRequest(id_token = idToken, fcm_token = fcmToken)
                val response = RetrofitInstance.api.loginUser(loginRequest)

                withContext(Dispatchers.Main) {
                    when (response.code()) {
                        201, 200 -> { // 로그인 성공
                            val loginResponse = response.body()
                            val accessToken = loginResponse?.access_token // 서버에서 발급한 access_token
                            val refreshToken = loginResponse?.refresh_token

                            if (accessToken != null) {
                                Log.d("AccessToken", "Access Token: $accessToken")
                                // access_token을 이용한 추가 작업 처리
                                saveTokens(loginResponse.access_token, loginResponse.refresh_token)
                                RetrofitInstance.setTokens(accessToken, refreshToken)
                                navigateToNextScreen(loginResponse?.access_token)
                            } else {
                                Log.e("AccessTokenError", "Access Token is null")
                            }
                        }
                        else -> { // 다른 오류 처리
                            Log.e("LoginError", "Login failed: ${response.message()}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginError", "Exception during login: ${e.message}")
                }
            }
        }
    }

    private suspend fun checkUserProfile(accessToken: String?): Boolean {
        return try {
            val response = accessToken?.let { RetrofitInstance.api.checkUserProfile(it) }
            when (response?.code()) {
                200 -> {
                    val body = response.body()
                    if (body is JsonObject) {
                        when {
                            body.has("message") && body["message"].asString == "유저 정보가 필요합니다." -> false
                            body.has("nickname") -> true
                            else -> false
                        }
                    } else {
                        false
                    }
                }
                401, 403 -> false
                else -> {
                    Log.e("ProfileCheck", "Unexpected response")
                    throw Exception("네트워크 에러")
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileCheck", "Error checking user profile: ${e.message}")
            false
        }
    }

    private fun navigateToNextScreen(accessToken: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val hasProfile = checkUserProfile(accessToken)
            withContext(Dispatchers.Main) {
                if (hasProfile) {
                    // 프로필이 설정되어 있으면 MainActivity로 이동
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // 프로필 설정이 필요하면 UserSettingsActivity로 이동
                    val intent = Intent(this@LoginActivity, UserSettingsActivity::class.java)
                    startActivity(intent)
                }
                finish()
            }
        }
    }
}