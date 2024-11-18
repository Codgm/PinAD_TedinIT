package com.pinAD.pinAD_fe.Profile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.Login_Sign.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.pinAD.pinAD_fe.Login_Sign.GlobalApplication.GlobalApplication
import com.pinAD.pinAD_fe.MainActivity
import com.pinAD.pinAD_fe.Profile.notification.NotificationDialogFragment
import com.pinAD.pinAD_fe.network.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.HttpException
import java.util.Locale


class SettingFragment : Fragment() {

    private lateinit var switchNotification: SwitchCompat
    private lateinit var tvTheme: TextView
    private lateinit var tvInterestsSetting: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnDeleteAccount: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnBussinessAccount: Button

    private lateinit var currentUserUid: String
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth : FirebaseAuth

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tvLanguage: TextView
    private var isBusinessUser: Boolean = false
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var seekBarRadius: SeekBar
    private lateinit var tvRadiusValue: TextView
    private var currentRadius: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

//        loadNotificationSettings()


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        auth = FirebaseAuth.getInstance()

        // 뷰 초기화
        btnBack = view.findViewById(R.id.btnBack)
        switchNotification = view.findViewById(R.id.switchNotification)
        tvTheme = view.findViewById(R.id.tvTheme)
        tvInterestsSetting = view.findViewById(R.id.tvInterestsSetting)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount)
        btnBussinessAccount = view.findViewById(R.id.btnSwitchToBusinessAccount)
        seekBarRadius = view.findViewById(R.id.seekBarRadius)
        tvRadiusValue = view.findViewById(R.id.tvRadiusValue)

        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        switchNotification.isChecked = notificationManager.areNotificationsEnabled()

        // SharedPreferences 초기화
        sharedPreferences = requireActivity().getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE)
        Log.d("LanguageSettings", "$sharedPreferences")
        tvLanguage = view.findViewById(R.id.tvLanguage)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        loadLanguagePreference()

        lifecycleScope.launch {
            UserDataManager.getUserData(true)?.let { profileData ->
                // 서버에서 받은 미터 단위를 SeekBar 단위로 변환
                currentRadius = convertMetersToProgress(profileData.radius ?: 100)
                seekBarRadius.progress = currentRadius
                updateRadiusText(currentRadius)
            }
        }

        // 언어 변경 클릭 리스너
        tvLanguage.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // 초기 테마 설정
        updateTheme()

        // 뒤로가기 버튼 리스너
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != notificationManager.areNotificationsEnabled()) {
                // 시스템 알림 설정 화면으로 이동
                val intent = Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                startActivity(intent)
            }
        }

        // 알림 설정 스위치 리스너
//        switchNotification.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                enablePushNotifications()
//            } else {
//                disablePushNotifications()
//            }
//        }

        seekBarRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // SeekBar의 progress가 변경될 때마다 반경 텍스트 업데이트
                updateRadiusText(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // SeekBar를 조작하기 시작할 때 호출
                currentRadius = seekBar?.progress ?: 0  // 현재 반경 값을 저장
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // SeekBar를 멈출 때 변경된 값에 대해 확인 메시지 표시
                showSaveCancelDialog(seekBar?.progress ?: 0)
            }
        })

        btnBussinessAccount.setOnClickListener {
            checkBusinessAccountStatus()
        }

//        // 테마 변경 클릭 리스너
//        tvTheme.setOnClickListener {
//            showThemeSelectionDialog()
//        }

//        // 관심사 변경 클릭 리스너
//        tvInterestsSetting.setOnClickListener {
//            showInterestsInputDialog()
//        }

        // 로그아웃 버튼 클릭 리스너
        btnLogout.setOnClickListener {
            logout()
        }

        // 회원 탈퇴 버튼 클릭 리스너
        btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        return view
    }

    private fun loadLanguagePreference() {
        val selectedLanguage = sharedPreferences.getString("language", "en")
        tvLanguage.text = selectedLanguage?.let { getLanguageName(it) }
    }

    private fun getLanguageName(langCode: String): String {
        return when (langCode) {
            "en" -> "English"
            "ko-rKR" -> "한국어"
            else -> langCode
        }
    }

    private fun showLanguageSelectionDialog() {
        val currentLanguage = sharedPreferences.getString(GlobalApplication.LANGUAGE_KEY, "en")
        val languageOptions = arrayOf("English", "한국어")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Language")
            .setSingleChoiceItems(languageOptions, if (currentLanguage == "ko-rKR") 1 else 0) { dialog, which ->
                val selectedLanguage = if (which == 1) "ko-rKR" else "en"
                saveLanguagePreference(selectedLanguage)
                dialog.dismiss()
                // 언어 변경 후 앱 재시작
                restartApp()
            }
            .create()
            .show()
    }

    private fun restartApp() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun saveLanguagePreference(language: String) {
        sharedPreferences.edit()
            .putString(GlobalApplication.LANGUAGE_KEY, language)
            .apply()
    }

    private fun convertProgressToMeters(progress: Int): Int {
        return progress * 100  // 100m 단위로 변환
    }

    // 미터 단위를 SeekBar의 progress 값으로 변환
    private fun convertMetersToProgress(meters: Int): Int {
        return (meters / 100).coerceIn(1, 20)  // 1~20 범위로 제한
    }

    @SuppressLint("StringFormatInvalid")
    private fun showSaveCancelDialog(newProgress: Int) {
        val meters = convertProgressToMeters(newProgress)  // 변경된 progress 값을 미터로 변환
        val radiusText = if (meters >= 1000) {
            getString(R.string.radius_value_format_km, meters / 1000.0)
        } else {
            getString(R.string.radius_value_format_m, meters)
        }

        // 다이얼로그 표시
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.radius_setting))  // 제목
            .setMessage(getString(R.string.confirm_save_radius, radiusText))  // 메시지
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                // 저장 버튼 클릭 시 서버에 반경 업데이트 요청
                updateRadius(newProgress)
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                // 취소 버튼 클릭 시 SeekBar progress 되돌리기
                seekBarRadius.progress = currentRadius
            }
            .show()
    }

    private fun updateRadiusText(progress: Int) {
        val meters = convertProgressToMeters(progress)
        val text = if (meters >= 1000) {
            getString(R.string.radius_value_format_km, meters / 1000.0)
        } else {
            getString(R.string.radius_value_format_m, meters)
        }
        tvRadiusValue.text = text
    }

    fun createUserDataRequestBody(notification_radius: Int): RequestBody {
        val userDataMap = mapOf("notification_radius" to notification_radius)  // Map으로 감싸기
        val json = Gson().toJson(userDataMap)  // Map을 JSON으로 변환
        return RequestBody.create("application/json".toMediaTypeOrNull(), json)  // RequestBody로 변환
    }

    private fun updateRadius(progress: Int) {
        val meters = convertProgressToMeters(progress)
        val userData = createUserDataRequestBody(meters)
        val token = RetrofitInstance.getAccessToken()
        Log.d("AccessToken", "Token: $token")

        lifecycleScope.launch {
            try {
                if (token != null) {
                    val response = RetrofitInstance.api.updateRadius("Bearer $token", userData)

                    if (response.isSuccessful) {
                        // 성공적으로 업데이트된 경우
                        Toast.makeText(context, getString(R.string.radius_update_success), Toast.LENGTH_SHORT).show()
                        // UserDataManager의 캐시된 데이터 업데이트
                        UserDataManager.getUserData(true)
                        currentRadius = progress  // 새로운 반경 값 저장
                    } else {
                        // 업데이트 실패
                        Toast.makeText(context, getString(R.string.radius_update_failed), Toast.LENGTH_SHORT).show()
                        // 이전 값으로 되돌리기
                        seekBarRadius.progress = currentRadius
                    }
                } else {
                    Toast.makeText(context, "token error", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리
                Log.e("UpdateRadiusError", "Error: ${e.message}")
                Toast.makeText(context, getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                seekBarRadius.progress = currentRadius
            }
        }
    }


    private fun checkBusinessAccountStatus() {
        Log.d("isBussinessUser", "$isBusinessUser")
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.checkBusiness()
                if (response.isSuccessful) {
                    val serverResponse = response.body() ?: false

                    // 현재 isBusinessUser 상태에 따른 처리
                    if (!isBusinessUser) { // 원래 계정이 일반 계정인 경우
                        if (serverResponse) {
                            // 서버에서 비즈니스 계정으로 전환 가능한 상태로 응답한 경우
                            Toast.makeText(context, getString(R.string.business_account_success), Toast.LENGTH_SHORT).show()
                            isBusinessUser = true
                            sharedViewModel.setBusinessUser(isBusinessUser)
                            Log.d("isBussinessUser", "$isBusinessUser")

                        } else {
                            // 비즈니스 계정으로 전환 불가 응답인 경우
                            Toast.makeText(
                                context,
                                getString(R.string.business_account_failure),
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("isBussinessUser", "$isBusinessUser")
                        }
                    } else { // 원래 계정이 비즈니스 계정인 경우
                        if (serverResponse) {
                            // 계속 비즈니스 계정 유지 (true인 경우)
                            Toast.makeText(context, getString(R.string.business_account_revert), Toast.LENGTH_SHORT).show()
                            isBusinessUser = false // 일반 계정으로 변경
                            sharedViewModel.setBusinessUser(isBusinessUser)
                            Log.d("isBussinessUser", "$isBusinessUser")
                        } else {
                            // 서버에서 여전히 비즈니스 계정으로 반환한 경우 (false)
                            Toast.makeText(context, getString(R.string.business_account_maintained), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, getString(R.string.business_account_check_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.network_error), Toast.LENGTH_SHORT).show()
            }
        }
    }



//    private fun loadNotificationSettings() {
//        // 서버에서 푸시 알림 설정을 불러와 switch 상태 설정
//        val accessToken = RetrofitInstance.getAccessToken()// 로그인 시 받은 accessToken
//            apiService.getNotificationSettings("Bearer $accessToken").enqueue(object : Callback<NotificationSettingsResponse> {
//                override fun onResponse(call: Call<NotificationSettingsResponse>, response: Response<NotificationSettingsResponse>) {
//                    if (response.isSuccessful) {
//                        val notificationEnabled = response.body()?.isEnabled ?: false
//                        switchNotification.isChecked = notificationEnabled
//                    }
//                }
//
//                override fun onFailure(call: Call<NotificationSettingsResponse>, t: Throwable) {
//                    Log.e("Settings", "Failed to load notification settings", t)
//                }
//            })
//    }
//
//    private fun enablePushNotifications() {
//        val accessToken = RetrofitInstance.getAccessToken()
//            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val fcmToken = task.result
//                    apiService.enablePushNotifications("Bearer $accessToken", fcmToken).enqueue(object : Callback<ResponseBody> {
//                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                            if (response.isSuccessful) {
//                                Log.d("Settings", "Push notifications enabled")
//                            }
//                        }
//
//                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                            Log.e("Settings", "Failed to enable push notifications", t)
//                        }
//                    })
//                }
//            }
//    }
//
//    private fun disablePushNotifications() {
//        val accessToken = RetrofitInstance.getAccessToken()
//            apiService.disablePushNotifications("Bearer $accessToken").enqueue(object :
//                Callback<ResponseBody> {
//                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                    if (response.isSuccessful) {
//                        Log.d("Settings", "Push notifications disabled")
//                    }
//                }
//
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    Log.e("Settings", "Failed to disable push notifications", t)
//                }
//            })
//    }

    private fun logout() {
        auth.signOut()
        // Google 로그아웃 처리
        googleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 로그아웃 완료 후
                RetrofitInstance.setTokens(null, null)

                // SharedPreferences에서 사용자 관련 데이터 제거
                val preferences = requireActivity().getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE)
                preferences.edit().apply {
                    remove("ACCESS_TOKEN")
                    remove("REFRESH_TOKEN")
                    // 필요한 경우 다른 사용자 관련 데이터도 제거
                    apply()
                }

                UserDataManager.clearCache()

                Toast.makeText(requireContext(), getString(R.string.logged_out), Toast.LENGTH_SHORT).show()
                val intent = Intent(activity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                activity?.finish()
            } else {
                // 로그아웃 실패 처리 (원하는 대로 처리 가능)
                Toast.makeText(requireContext(), getString(R.string.logout_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showThemeSelectionDialog() {
        val currentTheme = sharedPreferences.getString("theme", "light")
        val themeOptions = arrayOf("Light", "Dark")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Theme")
            .setSingleChoiceItems(themeOptions, if (currentTheme == "dark") 1 else 0) { dialog, which ->
                val selectedTheme = if (which == 1) "dark" else "light"
                saveThemePreference(selectedTheme)
                updateTheme()
                dialog.dismiss() // 선택 후 다이얼로그 닫기
                // 테마 변경 후 Activity 재시작
                requireActivity().recreate()
            }
            .create()
            .show()
    }

    private fun showInterestsInputDialog() {
        val currentInterests = tvInterestsSetting.text.toString()
        val input = EditText(requireContext()).apply {
            setText(currentInterests)
            hint = "Enter your interests"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Interests")
            .setView(input)
            .setPositiveButton("Save") { dialog, _ ->
                val newInterests = input.text.toString()
                tvInterestsSetting.text = newInterests
//                saveInterestsToDatabase(newInterests)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun saveThemePreference(theme: String) {
        sharedPreferences.edit().putString("theme", theme).apply()
        tvTheme.text = theme.replaceFirstChar { it.uppercase() }   // 설정 화면에서 현재 선택된 테마를 반영
    }

    private fun updateTheme() {
        val theme = sharedPreferences.getString("theme", "light")
        val mode = if (theme == "dark") AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
        tvTheme.text = theme?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

//    private fun saveInterestsToDatabase(interests: String) {
//        databaseReference.child(currentUserUid).child("interests").setValue(interests).addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                Toast.makeText(requireContext(), "Interests Updated", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(requireContext(), "Failed to Update Interests", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    private fun showDeleteAccountConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_delete_account))
            .setMessage(getString(R.string.delete_account_message))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                deleteUserAccount()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteUserAccount() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitInstance.api.deleteUser()
                if (response.isSuccessful) {
                    if (response.code() == 204) {
                        // 204 응답일 경우 로그인 화면으로 이동
                        Toast.makeText(requireContext(), getString(R.string.account_deleted), Toast.LENGTH_SHORT).show()
                        logout()
                        // 필요시 사용자 데이터 정리 (아직 안짜짐)
                        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                        activity?.finish()  // 현재 액티비티 종료
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.delete_account_failed), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.delete_account_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                Toast.makeText(requireContext(), "Error: ${e.message()}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
