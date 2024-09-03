package com.example.mappin_fe.Login_Sign.GlobalApplication

import android.app.Application
import com.example.mappin_fe.R
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, getString(R.string.kakao_app_key))

        // Naver SDK 초기화
        NaverIdLoginSDK.initialize(this, "dXtaisFDzZtaLMIyRRtW", "b7cyVWDy7b", "Mappin")
    }
}