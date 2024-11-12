package com.pinAD.pinAD_fe.Profile

import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.Login_Sign.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.pinAD.pinAD_fe.Login_Sign.GlobalApplication.GlobalApplication
import com.pinAD.pinAD_fe.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Locale


class SettingFragment : Fragment() {

    private lateinit var switchNotification: SwitchCompat
    private lateinit var tvTheme: TextView
    private lateinit var tvInterestsSetting: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnDeleteAccount: Button
    private lateinit var btnBack: ImageButton

    private lateinit var currentUserUid: String
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth : FirebaseAuth

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tvLanguage: TextView

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


        // SharedPreferences 초기화
        sharedPreferences = requireActivity().getSharedPreferences("LanguageSettings", Context.MODE_PRIVATE)
        Log.d("LanguageSettings", "$sharedPreferences")
        tvLanguage = view.findViewById(R.id.tvLanguage)

        loadLanguagePreference()

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

        // 알림 설정 스위치 리스너
        switchNotification.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                enablePushNotifications()
//            } else {
//                disablePushNotifications()
//            }
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
        googleSignInClient.revokeAccess().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 로그아웃 완료 후
                RetrofitInstance.setTokens(null, null)

                // SharedPreferences에서 사용자 관련 데이터 제거
                val preferences = requireActivity().getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE)
                preferences.edit().apply {
                    remove("user_token")
                    remove("ACCESS_TOKEN")
                    remove("REFRESH_TOKEN")
                    // 필요한 경우 다른 사용자 관련 데이터도 제거
                    apply()
                }

                Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
                val intent = Intent(activity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                activity?.finish()
            } else {
                // 로그아웃 실패 처리 (원하는 대로 처리 가능)
                Toast.makeText(requireContext(), "로그아웃 실패", Toast.LENGTH_SHORT).show()
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
            .setTitle("Confirm Account Deletion")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone and you will lose all your pins, stories, and other data.")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteUserAccount()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
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
                        Toast.makeText(requireContext(), "Account Deleted", Toast.LENGTH_SHORT).show()
                        logout()
                        // 필요시 사용자 데이터 정리 (아직 안짜짐)
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        startActivity(intent)
                        activity?.finish()  // 현재 액티비티 종료
                    } else {
                        Toast.makeText(requireContext(), "Failed to Delete Account", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to Delete Account", Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                Toast.makeText(requireContext(), "Error: ${e.message()}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
