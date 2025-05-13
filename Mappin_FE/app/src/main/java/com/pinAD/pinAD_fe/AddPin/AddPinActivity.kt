package com.pinAD.pinAD_fe.AddPin

import android.os.Bundle
import com.pinAD.pinAD_fe.AddPin.Camera.CameraFragment
import com.pinAD.pinAD_fe.BaseActivity
import com.pinAD.pinAD_fe.R

class AddPinActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pin)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CameraFragment()) // CameraFragment로 설정
                .commit()
        }
    }
}
