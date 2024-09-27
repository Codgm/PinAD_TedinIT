package com.example.mappin_fe.Profile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.mappin_fe.R
import com.example.mappin_fe.Login_Sign.LoginActivity
import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase

class SettingFragment : Fragment() {

    private lateinit var switchNotification: Switch
    private lateinit var tvTheme: TextView
    private lateinit var tvInterestsSetting: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnDeleteAccount: Button
    private lateinit var btnBack: ImageButton

//    private lateinit var firebaseAuth: FirebaseAuth
//    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUserUid: String

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        // 뷰 초기화
        btnBack = view.findViewById(R.id.btnBack)
        switchNotification = view.findViewById(R.id.switchNotification)
        tvTheme = view.findViewById(R.id.tvTheme)
        tvInterestsSetting = view.findViewById(R.id.tvInterestsSetting)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount)

        // Firebase 초기화
//        firebaseAuth = FirebaseAuth.getInstance()
//        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
//        currentUserUid = firebaseAuth.currentUser?.uid ?: ""

        // SharedPreferences 초기화
        sharedPreferences = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        // 초기 테마 설정
        updateTheme()

        // 뒤로가기 버튼 리스너
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 알림 설정 스위치 리스너
        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), "Notifications: ${if (isChecked) "On" else "Off"}", Toast.LENGTH_SHORT).show()
        }

        // 테마 변경 클릭 리스너
        tvTheme.setOnClickListener {
            showThemeSelectionDialog()
        }

        // 관심사 변경 클릭 리스너
        tvInterestsSetting.setOnClickListener {
            showInterestsInputDialog()
        }

        // 로그아웃 버튼 클릭 리스너
//        btnLogout.setOnClickListener {
//            firebaseAuth.signOut()
//            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
//            val intent = Intent(activity, LoginActivity::class.java)
//            startActivity(intent)
//            activity?.finish() // 현재 액티비티 종료
//        }

        // 회원 탈퇴 버튼 클릭 리스너
        btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        return view
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
//                deleteUserAccount()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

//    private fun deleteUserAccount() {
//        // 데이터베이스에서 사용자 정보 삭제
//        databaseReference.child(currentUserUid).removeValue().addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                // 사용자 계정 삭제
//                firebaseAuth.currentUser?.delete()?.addOnCompleteListener { deleteTask ->
//                    if (deleteTask.isSuccessful) {
//                        Toast.makeText(requireContext(), "Account Deleted", Toast.LENGTH_SHORT).show()
//                        val intent = Intent(activity, LoginActivity::class.java)
//                        startActivity(intent)
//                        activity?.finish() // 현재 액티비티 종료
//                    } else {
//                        Toast.makeText(requireContext(), "Failed to Delete Account", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } else {
//                Toast.makeText(requireContext(), "Failed to Delete User Data", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
}
