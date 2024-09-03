package com.example.mappin_fe.Login_Sign.GlobalApplication

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "607b30fba1f577a8f048920ea64a6d50")

        // Naver SDK 초기화
        NaverIdLoginSDK.initialize(this, "dXtaisFDzZtaLMIyRRtW", "b7cyVWDy7b", "Mappin")
    }
}