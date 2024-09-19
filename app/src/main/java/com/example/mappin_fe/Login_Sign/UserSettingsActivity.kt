package com.example.mappin_fe.Login_Sign

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.mappin_fe.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.appcompat.app.AppCompatActivity
import com.example.mappin_fe.Data.UserAccount
import com.example.mappin_fe.MainActivity

class UserSettingsActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

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

        firebaseAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().getReference("Users")

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
        val userId = firebaseAuth.currentUser?.uid
        val nickname = etNickname.text.toString().trim()
        val interests = getSelectedInterests()

        if (userId != null) {
            if (nickname.isNotEmpty()) {
                val userAccount = UserAccount(
                    idToken = firebaseAuth.currentUser?.getIdToken(false)?.result?.token,
                    emailId = firebaseAuth.currentUser?.email,
                    password = null,
                    nickname = nickname,
                    interests = interests
                )

                databaseRef.child(userId).setValue(userAccount)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // 설정이 완료되었음을 SharedPreferences에 저장
                            val sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isSettingsCompleted", true)
                            editor.apply()

                            Toast.makeText(this, "Settings Updated", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to Update Settings: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter a nickname", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getSelectedInterests(): String {
        val interests = mutableListOf<String>()
        for (i in 0 until chipGroupInterests.childCount) {
            val chip = chipGroupInterests.getChildAt(i) as Chip
            if (chip.isChecked || chip.tag as? Boolean == true) {
                interests.add(chip.text.toString())
            }
        }
        return interests.joinToString(", ")
    }
}
