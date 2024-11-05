package com.pinAD.pinAD_fe

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.pinAD.pinAD_fe.AddPin.AddPinActivity
import com.pinAD.pinAD_fe.Home.HomeFragment
import com.pinAD.pinAD_fe.HotPin.HotPinFragment
import com.pinAD.pinAD_fe.Profile.ProfileFragment
import com.pinAD.pinAD_fe.Search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pinAD.pinAD_fe.user_setting.UserSettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var addPinLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        // 테마를 super.onCreate() 전에 설정
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 초기화 블록을 try-catch로 감싸서 안전하게 처리
        try {
            initializeApp(savedInstanceState)
        } catch (e: Exception) {
            e.printStackTrace()
            handleInitializationError()
        }
    }

    private fun initializeApp(savedInstanceState: Bundle?) {
        // 사용자 설정 확인
//        checkUserSettings()

        // UI 초기화
        initializeUI(savedInstanceState)

        // ActivityResultLauncher 초기화
        initializeActivityLauncher()
    }

    private fun checkUserSettings() {
        val userSettings = getSharedPreferences("UserSettings", MODE_PRIVATE)
        val isSettingsComplete = userSettings.getBoolean("settings_complete", false)

        if (!isSettingsComplete) {
            navigateToUserSettings()
        }
    }

    private fun initializeUI(savedInstanceState: Bundle?) {
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // 첫 화면 설정
        val fromAddPin = intent?.getBooleanExtra("FROM_ADD_PIN", false) ?: false
        when {
            fromAddPin -> {
                replaceFragment(HomeFragment())
                bottomNavigationView.selectedItemId = R.id.navigation_home
            }
            savedInstanceState == null -> replaceFragment(HomeFragment())
        }

        setupBottomNavigation()
    }

    private fun initializeActivityLauncher() {
        addPinLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                replaceFragment(HomeFragment())
                bottomNavigationView.selectedItemId = R.id.navigation_home
            }
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> navigateToFragment(HomeFragment())
                R.id.navigation_search -> navigateToFragment(SearchFragment())
                R.id.navigation_add_pin -> {
                    navigateToAddPinActivity()
                    true
                }
                R.id.navigation_hot_pin -> navigateToFragment(HotPinFragment())
                R.id.navigation_profile -> navigateToFragment(ProfileFragment())
                else -> false
            }
        }
    }

    private fun navigateToFragment(fragment: Fragment): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.main_body_container)
        if (currentFragment?.javaClass != fragment.javaClass) {
            replaceFragment(fragment)
        }
        return true
    }

    private fun navigateToAddPinActivity() {
        val intent = Intent(this, AddPinActivity::class.java)
        addPinLauncher.launch(intent)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_body_container, fragment)
            .commit()
    }

    private fun navigateToUserSettings() {
        startActivity(Intent(this, UserSettingsActivity::class.java))
        finish()
    }

    private fun handleInitializationError() {
        navigateToUserSettings()
    }
}