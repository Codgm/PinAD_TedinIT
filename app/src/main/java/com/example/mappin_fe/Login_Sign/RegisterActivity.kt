package com.example.mappin_fe.Login_Sign

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mappin_fe.Data.RegisterAccount
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.Data.UserAccount
import com.example.mappin_fe.R
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
            Toast.makeText(this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "비밀번호는 최소 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
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
                        "회원가입이 완료되었습니다. 이메일을 확인해주세요.",
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
                                errorString.contains("email") -> "이미 등록된 이메일입니다"
                                errorString.contains("password") -> "비밀번호 형식이 올바르지 않습니다"
                                else -> "회원가입에 실패했습니다. 다시 시도해주세요"
                            }
                            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "오류가 발생했습니다. 다시 시도해주세요.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(
                    this@RegisterActivity,
                    "네트워크 오류가 발생했습니다. 연결을 확인해주세요.",
                    Toast.LENGTH_LONG
                ).show()
                t.printStackTrace()
            }
        })
    }
}