package com.example.mappin_fe.Login_Sign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.mappin_fe.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import androidx.appcompat.app.AppCompatActivity
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.Data.UserAccount
import com.example.mappin_fe.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserSettingsActivity : AppCompatActivity() {
    private var accessToken: String? = null

    private lateinit var etNickname: EditText
    private lateinit var chipGroupInterests: ChipGroup
    private lateinit var etAddInterest: EditText
    private lateinit var btnAddInterest: Button
    private lateinit var btnDone: Button

    // Define color resources for chips
    private val selectedColor = R.color.colorAccent // Selected chip color
    private val defaultColor = R.color.colorChipBackground // Default chip color

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)

        // Intent로부터 access_token을 받음
        accessToken = intent.getStringExtra("ACCESS_TOKEN")
        Log.d("token", "$accessToken")

        etNickname = findViewById(R.id.et_nickname)
        chipGroupInterests = findViewById(R.id.chip_group_interests)
        etAddInterest = findViewById(R.id.et_add_interest)
        btnAddInterest = findViewById(R.id.btn_add_interest)
        btnDone = findViewById(R.id.btn_done)

        // Add click listener for "Add" button
        btnAddInterest.setOnClickListener {
            addNewInterest()
        }

        // Add click listener for "Done" button
        btnDone.setOnClickListener {
            Log.d("UserSettingsActivity", "Done button clicked")
            saveUserSettings()
        }

        // Initialize Chip click listeners and colors
        initializeChips()
    }

    private fun initializeChips() {
        for (i in 0 until chipGroupInterests.childCount) {
            val chip = chipGroupInterests.getChildAt(i) as Chip
            // Initialize existing chips with click listeners
            chip.setOnClickListener {
                toggleChipSelection(chip)
            }
            // Set initial color based on selection state
            chip.setChipBackgroundColorResource(defaultColor)
            chip.isChecked = false
            chip.tag = false
        }
    }

    private fun addNewInterest() {
        val newInterest = etAddInterest.text.toString().trim()
        if (newInterest.isNotEmpty()) {
            val newChip = Chip(this).apply {
                text = newInterest
                isCloseIconVisible = true
                setChipBackgroundColorResource(selectedColor) // 선택된 색상으로 변경
                isClickable = true
                isCheckable = false
                tag = true // 선택된 상태로 태그 설정
                setOnCloseIconClickListener {
                    chipGroupInterests.removeView(this)
                }
                setOnClickListener {
                    toggleChipSelection(this)
                }
            }
            chipGroupInterests.addView(newChip)
            etAddInterest.text.clear()
        } else {
            Toast.makeText(this, "Please enter an interest", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleChipSelection(chip: Chip) {
        val isChecked = chip.tag as? Boolean ?: false
        if (isChecked) {
            // Deselect the chip
            chip.setChipBackgroundColorResource(defaultColor) // Default color for deselected
            chip.tag = false
        } else {
            // Select the chip
            chip.setChipBackgroundColorResource(selectedColor) // Color for selected
            chip.tag = true
        }
    }

    private fun saveUserSettings() {
        val accessToken = accessToken
        val nickname = etNickname.text.toString().trim()
        val interests = getSelectedInterests()

        Log.d("saveUserSettings", "nickname = $nickname, interests = $interests")

        if (nickname.isNotEmpty()) {
            val userAccount = UserAccount(
                nickname = nickname,
                tags = interests
            )
            Log.d("saveUserSettings", "userAccount 생성됨 = $userAccount")

            // 서버에 사용자 설정 저장
            CoroutineScope(Dispatchers.Main).launch {
                Log.d("saveUserSettings", "서버 요청 시작")
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitInstance.api.updateUserSettings("Bearer $accessToken", userAccount)
                    }
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            // Save the completed settings status to SharedPreferences
                            val sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE)
                            sharedPreferences.edit().apply {
                                putBoolean("isSettingsCompleted", true)
                                apply()
                            }

                            Toast.makeText(this@UserSettingsActivity, "Settings Updated Successfully", Toast.LENGTH_SHORT).show()

                            // Navigate to MainActivity
                            val intent = Intent(this@UserSettingsActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish() // Close the UserSettingsActivity
                        } else {
                            Toast.makeText(this@UserSettingsActivity, "Failed to Update Settings: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UserSettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please enter a nickname and ensure you're logged in", Toast.LENGTH_SHORT).show()
        }
    }



    private fun getSelectedInterests(): List<String> {
        val interests = mutableListOf<String>()
        for (i in 0 until chipGroupInterests.childCount) {
            val chip = chipGroupInterests.getChildAt(i) as Chip
            if (chip.isChecked || chip.tag as? Boolean == true) {
                interests.add(chip.text.toString())
            }
        }
        return interests
    }
}