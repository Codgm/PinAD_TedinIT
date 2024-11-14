package com.pinAD.pinAD_fe.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.pinAD.pinAD_fe.Login_Sign.LoginActivity
import com.pinAD.pinAD_fe.MainActivity
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.splash.screen.OnboardingActivity
import com.pinAD.pinAD_fe.network.RetrofitInstance

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val preferences = getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE)
        val isFirstRun = preferences.getBoolean("IS_FIRST_RUN", true)
        val savedAccessToken = preferences.getString("ACCESS_TOKEN", null)

        // RetrofitInstance에 저장된 토큰 설정
        if (savedAccessToken != null) {
            val savedRefreshToken = preferences.getString("REFRESH_TOKEN", null)
            RetrofitInstance.setTokens(savedAccessToken, savedRefreshToken)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivity = when {
                isFirstRun -> {
                    // 최초 실행 상태 업데이트
                    preferences.edit().putBoolean("IS_FIRST_RUN", false).apply()
                    OnboardingActivity::class.java
                }
                RetrofitInstance.getAccessToken() != null -> {
                    // RetrofitInstance의 토큰을 다시 한번 확인
                    val currentToken = RetrofitInstance.getAccessToken()
                    if (currentToken != savedAccessToken) {
                        // 토큰이 변경되었다면 SharedPreferences 업데이트
                        preferences.edit()
                            .putString("ACCESS_TOKEN", currentToken)
                            .apply()
                    }
                    MainActivity::class.java
                }
                else -> {
                    // 토큰이 없는 경우 SharedPreferences의 토큰도 제거
                    preferences.edit()
                        .remove("ACCESS_TOKEN")
                        .remove("REFRESH_TOKEN")
                        .apply()
                    LoginActivity::class.java
                }
            }

            val intent = Intent(this, nextActivity).apply {
                // 기존 액티비티 스택을 모두 제거하고 새로운 태스크 시작
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }, 3000)
    }
}