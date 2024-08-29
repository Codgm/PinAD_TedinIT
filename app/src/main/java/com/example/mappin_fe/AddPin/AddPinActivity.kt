package com.example.mappin_fe.AddPin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mappin_fe.AddPin.Camera.CameraFragment
import com.example.mappin_fe.R

class AddPinActivity : AppCompatActivity() {

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
