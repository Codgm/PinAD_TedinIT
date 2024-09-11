package com.example.mappin_fe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.mappin_fe.AddPin.AddPinActivity
import com.example.mappin_fe.Home.HomeFragment
import com.example.mappin_fe.HotPin.HotPinFragment
import com.example.mappin_fe.Login_Sign.UserSettingsActivity
import com.example.mappin_fe.Profile.ProfileFragment
import com.example.mappin_fe.Search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val frameId: Int by lazy { R.id.main_body_container }
    private val bottomNavigationView: BottomNavigationView by lazy { findViewById(R.id.bottom_navigation) }
    private lateinit var addPinLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ActivityResultLauncher 초기화
        addPinLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // 결과 처리 로직
            if (result.resultCode == RESULT_OK) {
                replaceFragment(HomeFragment())
                bottomNavigationView.selectedItemId = R.id.navigation_home
            }
        }

        // SharedPreferences 초기화 (중복 제거)
        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        // 테마 설정
        val theme = sharedPreferences.getString("theme", "light")
        val mode = if (theme == "dark") AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

        // 사용자 설정 완료 여부 확인
        val userSettings = getSharedPreferences("UserSettings", MODE_PRIVATE)
        val isSettingsCompleted = userSettings.getBoolean("isSettingsCompleted", false)

        if (!isSettingsCompleted) {
            // 설정이 완료되지 않았다면 UserSettingsActivity로 이동
            val intent = Intent(this, UserSettingsActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // 설정이 완료되었으면 메인 화면 표시
            setContentView(R.layout.activity_main)

            // 첫 화면 설정
            if (intent.getBooleanExtra("FROM_ADD_PIN", false)) {
                replaceFragment(HomeFragment())
                bottomNavigationView.selectedItemId = R.id.navigation_home
            } else if (savedInstanceState == null) {
                replaceFragment(HomeFragment())
            }

            // 하단 네비게이션 바 초기화 및 이벤트 설정
            bottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        navigateToFragment(HomeFragment())
                        true
                    }
                    R.id.navigation_search -> {
                        navigateToFragment(SearchFragment())
                        true
                    }
                    R.id.navigation_add_pin -> {
                        navigateToAddPinActivity()
                        true
                    }
                    R.id.navigation_hot_pin -> {
                        navigateToFragment(HotPinFragment())
                        true
                    }
                    R.id.navigation_profile -> {
                        navigateToFragment(ProfileFragment())
                        true
                    }
                    else -> false
                }
            }
        }
    }

    // 화면 전환 메소드 (중복 전환 방지)
    private fun navigateToFragment(fragment: Fragment) {
        // 현재 표시된 Fragment와 같은 Fragment로 전환을 시도하는 경우 무시
        val currentFragment = supportFragmentManager.findFragmentById(frameId)
        if (currentFragment?.javaClass == fragment.javaClass) return

        replaceFragment(fragment)
    }

    // AddPinActivity 전환 메소드
    private fun navigateToAddPinActivity() {
        val intent = Intent(this, AddPinActivity::class.java)
        addPinLauncher.launch(intent)
    }

    // 화면 전환 구현 메소드
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(frameId, fragment)
            .commit()
    }
}
