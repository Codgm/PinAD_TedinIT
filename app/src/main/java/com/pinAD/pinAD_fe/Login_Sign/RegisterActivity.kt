package com.pinAD.pinAD_fe.Login_Sign

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pinAD.pinAD_fe.Data.login_register.RegisterAccount
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // UI 요소 초기화
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_pwd)
        etConfirmPassword = findViewById(R.id.et_confirm_pwd)
        btnRegister = findViewById(R.id.btn_register)

        btnRegister.setOnClickListener {
            registerUser()
        }
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
            password = password,
//            password2 = confirmPassword
        )

        // API 호출
        RetrofitInstance.api.registerUser(registerAccount).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Your membership has been registered. Please check your email.",
                        Toast.LENGTH_LONG
                    ).show()

                    // 로그인 화면으로 이동
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
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