package com.example.mappin_fe.Login_Sign

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mappin_fe.Data.UserAccount
import com.example.mappin_fe.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etVerificationCode: EditText
    private lateinit var btnSendVerification: Button
    private lateinit var btnVerifyCode: Button
    private lateinit var btnRegister: Button

    private var verificationId: String? = null
    private var isPhoneVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().getReference("Users")

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_pwd)
        etConfirmPassword = findViewById(R.id.et_confirm_pwd)
        etPhoneNumber = findViewById(R.id.et_phone_number)
        etVerificationCode = findViewById(R.id.et_verification_code)
        btnSendVerification = findViewById(R.id.btn_send_verification)
        btnVerifyCode = findViewById(R.id.btn_verify_code)
        btnRegister = findViewById(R.id.btn_register)

        btnSendVerification.setOnClickListener {
            etVerificationCode.visibility = View.VISIBLE
            btnVerifyCode.visibility = View.VISIBLE
            sendVerificationCode()
        }

        btnVerifyCode.setOnClickListener {
            verifyCode()
        }

        btnRegister.setOnClickListener {
            if (isPhoneVerified) {
                registerUser()
            } else {
                Toast.makeText(this, "Please verify your phone number first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendVerificationCode() {
        val phoneNumber = etPhoneNumber.text.toString().trim()
        if (phoneNumber.isNotEmpty()) {
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        signInWithPhoneAuthCredential(credential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(this@RegisterActivity, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        this@RegisterActivity.verificationId = verificationId
                        Toast.makeText(this@RegisterActivity, "Verification code sent", Toast.LENGTH_SHORT).show()
                    }
                })
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyCode() {
        val code = etVerificationCode.text.toString().trim()
        if (code.isNotEmpty() && verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        } else {
            Toast.makeText(this, "Please enter the verification code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    isPhoneVerified = true
                    Toast.makeText(this, "Phone number verified", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Verification failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun registerUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid
                    if (userId != null) {
                        val user = UserAccount(idToken = userId, emailId = email, password = password, phoneNumber = phoneNumber)
                        databaseRef.child(userId).setValue(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}