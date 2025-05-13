package com.pinAD.pinAD_fe.splash.screen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.pinAD.pinAD_fe.Login_Sign.LoginActivity
import com.pinAD.pinAD_fe.R

class OnboardingActivity : FragmentActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.ViewPager)
        btnLogin = findViewById(R.id.btnLogin)

        viewPager.adapter = OnboardingPagerAdapter(this)

        btnLogin.visibility = View.GONE  // 처음에는 버튼을 숨깁니다.

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 4) {  // 마지막 페이지일 때
                    btnLogin.visibility = View.VISIBLE
                } else {
                    btnLogin.visibility = View.GONE
                }
            }
        })

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private inner class OnboardingPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment {
            return OnboardingFragment.newInstance(position)
        }
    }
}