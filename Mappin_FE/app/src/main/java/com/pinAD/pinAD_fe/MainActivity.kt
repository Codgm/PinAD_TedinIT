package com.pinAD.pinAD_fe

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.pinAD.pinAD_fe.AddPin.AddPinActivity
import com.pinAD.pinAD_fe.Home.HomeFragment
import com.pinAD.pinAD_fe.HotPin.HotPinFragment
import com.pinAD.pinAD_fe.Profile.ProfileFragment
import com.pinAD.pinAD_fe.Search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pinAD.pinAD_fe.Login_Sign.LoginActivity
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.network.UserDataManager
import com.pinAD.pinAD_fe.user_setting.UserSettingsActivity
import com.pinAD.pinAD_fe.utils.NotificationHelper
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : BaseActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var addPinLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 테마를 super.onCreate() 전에 설정
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannels(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationHelper.hasNotificationPermission(this)) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
        setContentView(R.layout.activity_main)

        val pinId = intent.getStringExtra("pin_id")

        // pin_id가 존재하면 HomeFragment에 전달하여 핀 정보를 보여줌
        if (pinId != null) {
            navigateToPinDetail(pinId)
        } else {
            // pin_id가 없으면 일반적으로 HomeFragment로 이동
            replaceFragment(HomeFragment())
        }

        // 초기화 블록을 try-catch로 감싸서 안전하게 처리
        try {
            initializeApp(savedInstanceState)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun navigateToPinDetail(pinId: String) {
        val homeFragment = HomeFragment.newInstance(pinId, true)
        replaceFragment(homeFragment)
    }

    private fun initializeApp(savedInstanceState: Bundle?) {
        // 사용자 설정 확인
        checkUser()

        // UI 초기화
        initializeUI(savedInstanceState)

        // ActivityResultLauncher 초기화
        initializeActivityLauncher()

        loadUserProfile()
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            UserDataManager.getUserData()?.let { userAccount ->
            } ?: run {
                Toast.makeText(baseContext, "profile load failed", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun checkUser() {
        val accessToken = RetrofitInstance.getAccessToken()
        if (accessToken == null) {
            Log.d("MainActivity", "ACCESS_TOKEN is missing. Redirecting to LoginActivity.")
            navigateToLoginActivity()
            return
        }
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인됨
                NotificationHelper.createNotificationChannels(this)
            }
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
}