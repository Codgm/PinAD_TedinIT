package com.pinAD.pinAD_fe.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.pinAD.pinAD_fe.Login_Sign.LoginActivity
import com.pinAD.pinAD_fe.MainActivity
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.splash.screen.OnboardingActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val preferences = getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE)
        val isFirstRun = preferences.getBoolean("IS_FIRST_RUN", true)
        val accessToken = preferences.getString("ACCESS_TOKEN", null)

        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivity = when {
                isFirstRun -> {
                    // 최초 실행 상태 업데이트
                    preferences.edit().putBoolean("IS_FIRST_RUN", false).apply()
                    OnboardingActivity::class.java
                }
                accessToken != null -> MainActivity::class.java
                else -> LoginActivity::class.java
            }
            startActivity(Intent(this, nextActivity))
            finish()
        }, 3000)
    }
}