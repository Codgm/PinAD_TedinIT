package com.pinAD.pinAD_fe.Login_Sign.GlobalApplication

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.pinAD.pinAD_fe.R
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK
import com.pinAD.pinAD_fe.utils.NotificationHelper
import java.util.Locale

class GlobalApplication : Application() {
    companion object {
        const val LANGUAGE_PREF = "LanguageSettings"
        const val LANGUAGE_KEY = "language"
    }
    override fun onCreate() {
        super.onCreate()
        applyLanguageFromSettings()
        NotificationHelper.createNotificationChannels(this)
        FirebaseApp.initializeApp(this)
        try {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun applyLanguageFromSettings() {
        val sharedPreferences = getSharedPreferences(LANGUAGE_PREF, Context.MODE_PRIVATE)
        val language = sharedPreferences.getString(LANGUAGE_KEY, "en") ?: "en"
        updateLocale(language)
    }

    private fun updateLocale(language: String) {
        val locale = when (language) {
            "ko-rKR" -> Locale("ko", "KR")
            else -> Locale(language)
        }
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}