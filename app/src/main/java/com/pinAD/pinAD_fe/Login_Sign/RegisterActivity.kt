package com.pinAD.pinAD_fe.Login_Sign

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pinAD.pinAD_fe.Data.login_register.RegisterAccount
import com.pinAD.pinAD_fe.Data.login_register.VerificationResponse
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var emailVerifyButton: Button
    private lateinit var tokenEditText: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private var isEmailVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // UI 요소 초기화
        etEmail = findViewById(R.id.et_email)
        emailVerifyButton = findViewById(R.id.btn_email_verify)
        tokenEditText = findViewById(R.id.et_token)
        etPassword = findViewById(R.id.et_pwd)
        etConfirmPassword = findViewById(R.id.et_confirm_pwd)
        btnRegister = findViewById(R.id.btn_register)

        tokenEditText.visibility = View.GONE
        emailVerifyButton.visibility = View.GONE

        btnRegister.setOnClickListener {
            if (!isEmailVerified) {
                // 이메일 인증을 위한 토큰 입력 필드와 인증 버튼 표시
                tokenEditText.visibility = View.VISIBLE
                emailVerifyButton.visibility = View.VISIBLE
                registerUser()
            } else {
                // 이메일 인증이 완료되면
                Toast.makeText(
                    this@RegisterActivity,
                    "Your membership has been registered.",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()

            }
        }

        emailVerifyButton.setOnClickListener {
            val token = tokenEditText.text.toString().trim()
            if (token.isNotEmpty()) {
                verifyEmailToken(token)
            } else {
                Toast.makeText(this, "Please enter the verification token", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyEmailToken(token: String) {
        // 서버로 토큰을 보내 이메일 인증 진행
        RetrofitInstance.api.verifyEmailToken(token).enqueue(object : Callback<VerificationResponse> {
            override fun onResponse(call: Call<VerificationResponse>, response: Response<VerificationResponse>) {
                if (response.isSuccessful) {
                    isEmailVerified = true
                    Toast.makeText(this@RegisterActivity, "Email verified successfully", Toast.LENGTH_SHORT).show()
                    // 인증 성공 시 토큰 입력 필드와 버튼을 숨김
                    tokenEditText.visibility = View.GONE
                    emailVerifyButton.visibility = View.GONE
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to verify token"
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<VerificationResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Network error. Please try again.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun registerUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // 입력 유효성 검사
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show()
            return
        }

        // 회원가입 요청 객체 생성
        val registerAccount = RegisterAccount(
            email = email,
            password = password
        )

        // API 호출
        RetrofitInstance.api.registerUser(registerAccount).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 이메일 인증을 위한 토큰 입력 안내
                    Toast.makeText(
                        this@RegisterActivity,
                        "Please enter the token sent to your email for verification.",
                        Toast.LENGTH_LONG
                    ).show()

                    // 이메일 인증을 위한 입력 필드 및 버튼 보이기
                    tokenEditText.visibility = View.VISIBLE
                    emailVerifyButton.visibility = View.VISIBLE
                } else {
                    try {
                        // 에러 응답 처리
                        response.errorBody()?.string()?.let { errorString ->
                            val message = when {
                                errorString.contains("email") -> "This email is already registered"
                                errorString.contains("password") -> "Invalid password format"
                                else -> "Membership registration failed. Please try again"
                            }
                            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "An error has occurred. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.network_error),
                    Toast.LENGTH_LONG
                ).show()
                t.printStackTrace()
            }
        })
    }
}