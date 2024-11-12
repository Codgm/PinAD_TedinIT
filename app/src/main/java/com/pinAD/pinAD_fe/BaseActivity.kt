package com.pinAD.pinAD_fe

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pinAD.pinAD_fe.Login_Sign.GlobalApplication.GlobalApplication
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val sharedPreferences = newBase.getSharedPreferences(GlobalApplication.LANGUAGE_PREF, Context.MODE_PRIVATE)
        val language = sharedPreferences.getString(GlobalApplication.LANGUAGE_KEY, "en") ?: "en"
        val locale = when (language) {
            "ko-rKR" -> Locale("ko", "KR")
            else -> Locale(language)
        }
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 언어 설정이 변경될 때마다 리소스 업데이트
        updateResourcesLocale()
    }

    private fun updateResourcesLocale() {
        val sharedPreferences = getSharedPreferences(GlobalApplication.LANGUAGE_PREF, Context.MODE_PRIVATE)
        val language = sharedPreferences.getString(GlobalApplication.LANGUAGE_KEY, "en") ?: "en"
        val locale = when (language) {
            "ko-rKR" -> Locale("ko", "KR")
            else -> Locale(language)
        }
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}