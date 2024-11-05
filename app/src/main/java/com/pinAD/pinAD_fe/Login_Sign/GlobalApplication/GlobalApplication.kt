package com.pinAD.pinAD_fe.Login_Sign.GlobalApplication

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.pinAD.pinAD_fe.R
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}