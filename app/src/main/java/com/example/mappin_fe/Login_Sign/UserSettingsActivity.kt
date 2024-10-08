package com.example.mappin_fe.Login_Sign

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import android.widget.ViewFlipper
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

//    private lateinit var etNickname: EditText
//    private lateinit var chipGroupInterests: ChipGroup
    private lateinit var etAddInterest: EditText
//    private lateinit var btnAddInterest: Button
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var btnNext: Button
    private lateinit var btnPrevious: Button
    private val userResponses = mutableMapOf<String, Any>()

    companion object {
        private const val REQUEST_CODE_PRODUCT_IMAGE = 1001
        private const val REQUEST_CODE_RELATED_IMAGE = 1002
    }

    // Define color resources for chips
    private val selectedColor = R.color.colorAccent // Selected chip color
    private val defaultColor = R.color.colorChipBackground // Default chip color

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)


        // Intent로부터 access_token을 받음
        accessToken = intent.getStringExtra("ACCESS_TOKEN")
        Log.d("token", "$accessToken")

        viewFlipper = findViewById(R.id.viewFlipper)
        btnNext = findViewById(R.id.btnNext)
        btnPrevious = findViewById(R.id.btnPrevious)

        initializeViews()

        btnNext.setOnClickListener {
            if (viewFlipper.displayedChild < viewFlipper.childCount - 1) {
                saveCurrentPageResponse()
                viewFlipper.showNext()
                updateButtonVisibility()
            } else {
//                saveUserSettings()
            }
        }

        btnPrevious.setOnClickListener {
            if (viewFlipper.displayedChild > 0) {
                viewFlipper.showPrevious()
                updateButtonVisibility()
            }
        }

        updateButtonVisibility()

        // Add click listener for "Add" button
//        btnAddInterest.setOnClickListener {
//            addNewInterest()
//        }

//        // Initialize Chip click listeners and colors
//        initializeChips()
    }

    private fun initializeViews() {

        //1.Nickname
        val etNickname = findViewById<EditText>(R.id.etNickname)
        etNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userResponses["nickname"] = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 2. 성별 선택
        val genderRadioGroup = findViewById<RadioGroup>(R.id.radioGroupGender)
        genderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val gender = when (checkedId) {
                R.id.radioMale -> "남성"
                R.id.radioFemale -> "여성"
                R.id.radioOther -> "기타"
                else -> ""
            }
            userResponses["gender"] = gender
        }

        // 2. 연령대 선택
        val ageRadioGroup = findViewById<RadioGroup>(R.id.radioGroupAge)
        ageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val age = when (checkedId) {
                R.id.radio10s -> "10대"
                R.id.radio20s -> "20대"
                R.id.radio30s -> "30대"
                R.id.radio40s -> "40대"
                R.id.radio50sPlus -> "50대 이상"
                else -> ""
            }
            userResponses["age"] = age
        }

        // 3. 관심 쇼핑 품목
        val shoppingInterestsChipGroup = findViewById<ChipGroup>(R.id.chipGroupShoppingInterests)
        shoppingInterestsChipGroup.setOnCheckedChangeListener { group, _ ->
            val interests = group.checkedChipIds.map { id ->
                (group.findViewById<Chip>(id)).text.toString()
            }
            userResponses["shoppingInterests"] = interests
        }

        // 4. 주로 쇼핑하는 지역
        val shoppingAreasChipGroup = findViewById<ChipGroup>(R.id.chipGroupShoppingAreas)
        shoppingAreasChipGroup.setOnCheckedChangeListener { group, _ ->
            val areas = group.checkedChipIds.map { id ->
                (group.findViewById<Chip>(id)).text.toString()
            }
            userResponses["shoppingAreas"] = areas
        }

        // 5. 선호하는 브랜드
        val brandPreferencesChipGroup = findViewById<ChipGroup>(R.id.chipGroupPreferredBrands)
        brandPreferencesChipGroup.setOnCheckedChangeListener { group, _ ->
            val brands = group.checkedChipIds.map { id ->
                (group.findViewById<Chip>(id)).text.toString()
            }
            userResponses["brandPreferences"] = brands
        }

        // 6. 쇼핑 시 중요 요소
        val shoppingPrioritiesChipGroup = findViewById<ChipGroup>(R.id.chipGroupImportance)
        shoppingPrioritiesChipGroup.setOnCheckedChangeListener { group, _ ->
            val priorities = group.checkedChipIds.map { id ->
                (group.findViewById<Chip>(id)).text.toString()
            }
            userResponses["shoppingPriorities"] = priorities
        }

        // 7. 관심사 및 취미
        val hobbiesInterestsChipGroup = findViewById<ChipGroup>(R.id.chipGroupHobbiesInterests)
        hobbiesInterestsChipGroup.setOnCheckedChangeListener { group, _ ->
            val hobbies = group.checkedChipIds.map { id ->
                (group.findViewById<Chip>(id)).text.toString()
            }
            userResponses["hobbiesInterests"] = hobbies
        }

        // 8. 선호하는 오프라인 쇼핑 시간대
        val shoppingTimeRadioGroup = findViewById<RadioGroup>(R.id.radioGroupShoppingTime)
        shoppingTimeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val shoppingTime = when (checkedId) {
                R.id.radioMorning -> "오전 (6시~12시)"
                R.id.radioAfternoon -> "오후 (12시~18시)"
                R.id.radioEvening -> "저녁 (18시~21시)"
                R.id.radioNight -> "야간 (21시~24시)"
                R.id.radioAnyTime -> "제한 없음"
                else -> ""
            }
            userResponses["preferredShoppingTime"] = shoppingTime
        }

        // 9. 푸시 알림 수신 반경
        val radioGroupNotificationRadius = findViewById<RadioGroup>(R.id.radioGroupNotificationRadius)
        radioGroupNotificationRadius.setOnCheckedChangeListener { _, checkedId ->
            val selectedOption = when (checkedId) {
                R.id.radioButton100m -> "100m"
                R.id.radioButton200m -> "200m"
                R.id.radioButton500m -> "500m"
                R.id.radioButton1km -> "1km"
                R.id.radioButton2km -> "2km"
                R.id.radioButtonUnlimited -> "무제한"
                else -> ""
            }
            // 선택한 값을 userResponses에 저장
            userResponses["notificationRadius"] = selectedOption
        }

        // 10. 하루 최대 푸시 알림 개수
        val radioGroupMaxNotifications = findViewById<RadioGroup>(R.id.radioGroupMaxNotifications)
        radioGroupMaxNotifications.setOnCheckedChangeListener { _, checkedId ->
            val selectedOption = when (checkedId) {
                R.id.radioButton1 -> "1개"
                R.id.radioButton3 -> "3개"
                R.id.radioButton5 -> "5개"
                R.id.radioButton10 -> "10개"
                R.id.radioButtonAny -> "제한 없음"
                else -> ""
            }
            // 선택한 값을 userResponses에 저장
            userResponses["maxPushNotifications"] = selectedOption
        }

        // 11. 쿠폰 선물 대상
        val etCouponGift = findViewById<EditText>(R.id.etCouponGift)
        etCouponGift.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                userResponses["couponGift"] = etCouponGift.text.toString()
            }
        }

        // 12. 관심 제품 이미지 업로드
        val btnUploadProductImage = findViewById<Button>(R.id.btnUploadInterestImage)
        btnUploadProductImage.setOnClickListener {
            // TODO: Implement image upload functionality
            // 이미지 선택 후 업로드 로직을 구현해야 합니다.
            // 업로드된 이미지의 URL 또는 식별자를 userResponses에 저장합니다.
            uploadImage(isRelatedImage = false)
        }

        // 13. 관련 제품 이미지 업로드
        val btnUploadRelatedImage = findViewById<Button>(R.id.btnUploadRelatedImage)
        btnUploadRelatedImage.setOnClickListener {
            // TODO: Implement image upload functionality
            // 이미지 선택 후 업로드 로직을 구현해야 합니다.
            // 업로드된 이미지의 URL 또는 식별자를 userResponses에 저장합니다.
            uploadImage(isRelatedImage = true)
        }
    }

    private fun saveCurrentPageResponse() {
        // 현재 보여지는 페이지에 대한 사용자 입력을 저장하는 함수
        when (viewFlipper.displayedChild) {
            0 -> {
                // 닉네임 저장
                val nickname = findViewById<EditText>(R.id.etNickname).text.toString().trim()
                if (nickname.isNotEmpty()) {
                    userResponses["nickname"] = nickname
                }
            }
            1 -> {
                // 성별 저장
                val selectedGenderId = findViewById<RadioGroup>(R.id.radioGroupGender).checkedRadioButtonId
                val gender = if (selectedGenderId != -1) {
                    findViewById<RadioButton>(selectedGenderId).text.toString()
                } else {
                    null
                }
                gender?.let {
                    userResponses["gender"] = it
                }
            }
            2 -> {
                // 연령대 저장
                val selectedAgeGroupId = findViewById<RadioGroup>(R.id.radioGroupAge).checkedRadioButtonId
                val ageGroup = if (selectedAgeGroupId != -1) {
                    findViewById<RadioButton>(selectedAgeGroupId).text.toString()
                } else {
                    null
                }
                ageGroup?.let {
                    userResponses["age"] = it
                }
            }
            3 -> {
                // 관심 쇼핑 영역 저장
                val selectedInterests = findViewById<ChipGroup>(R.id.chipGroupShoppingInterests).checkedChipIds.map { id ->
                    findViewById<Chip>(id).text.toString()
                }
                if (selectedInterests.isNotEmpty()) {
                    userResponses["shoppingInterests"] = selectedInterests
                }
            }
            4 -> {
                // 쇼핑 지역 저장
                val selectedAreas = findViewById<ChipGroup>(R.id.chipGroupShoppingAreas).checkedChipIds.map { id ->
                    findViewById<Chip>(id).text.toString()
                }
                if (selectedAreas.isNotEmpty()) {
                    userResponses["shoppingAreas"] = selectedAreas
                }
            }
            5 -> {
                // 선호 브랜드 저장
                val selectedBrands = findViewById<ChipGroup>(R.id.chipGroupPreferredBrands).checkedChipIds.map { id ->
                    findViewById<Chip>(id).text.toString()
                }
                if (selectedBrands.isNotEmpty()) {
                    userResponses["brandPreferences"] = selectedBrands
                }
            }
            6 -> {
                // 쇼핑 우선순위 저장
                val selectedPriorities = findViewById<ChipGroup>(R.id.chipGroupImportance).checkedChipIds.map { id ->
                    findViewById<Chip>(id).text.toString()
                }
                if (selectedPriorities.isNotEmpty()) {
                    userResponses["shoppingPriorities"] = selectedPriorities
                }
            }
            7 -> {
                // 취미와 관심사 저장
                val selectedHobbies = findViewById<ChipGroup>(R.id.chipGroupHobbiesInterests).checkedChipIds.map { id ->
                    findViewById<Chip>(id).text.toString()
                }
                if (selectedHobbies.isNotEmpty()) {
                    userResponses["hobbiesInterests"] = selectedHobbies
                }
            }
            8 -> {
                // 선호하는 쇼핑 시간대 저장
                val selectedTimeId = findViewById<RadioGroup>(R.id.radioGroupShoppingTime).checkedRadioButtonId
                val preferredShoppingTime = if (selectedTimeId != -1) {
                    findViewById<RadioButton>(selectedTimeId).text.toString()
                } else {
                    null
                }
                preferredShoppingTime?.let {
                    userResponses["preferredShoppingTime"] = it
                }
            }
            9 -> {
                // 알림 반경 및 제한 저장
                val notificationRadiusId = findViewById<RadioGroup>(R.id.radioGroupNotificationRadius).checkedRadioButtonId
                val notificationRadius = if (notificationRadiusId != -1) {
                    findViewById<RadioButton>(notificationRadiusId).text.toString()
                } else {
                    null
                }
                notificationRadius?.let {
                    userResponses["notificationRadius"] = it
                }
            }
            10 -> {
                // 하루 최대 푸시 알림 개수 저장
                val maxPushNotificationsId = findViewById<RadioGroup>(R.id.radioGroupMaxNotifications).checkedRadioButtonId
                val maxPushNotifications = if (maxPushNotificationsId != -1) {
                    findViewById<RadioButton>(maxPushNotificationsId).text.toString()
                } else {
                    null
                }
                maxPushNotifications?.let {
                    userResponses["maxPushNotifications"] = it
                }
            }
            11 -> {
                // 쿠폰/선물 입력 저장
                val couponGift = findViewById<EditText>(R.id.etCouponGift).text.toString().trim()
                if (couponGift.isNotEmpty()) {
                    userResponses["couponGift"] = couponGift
                }
            }
            12 -> {
                // 관심 제품 이미지 업로드 저장
                val interestProductImageUri = findViewById<Button>(R.id.btnUploadInterestImage) // 이미지 URI 저장
                interestProductImageUri?.let {
                    userResponses["interestProductImage"] = it.toString()
                }
            }
            13 -> {
                // 관련 제품 이미지 업로드 저장
                val relatedProductImageUri = findViewById<Button>(R.id.btnUploadRelatedImage) // 이미지 URI 저장
                relatedProductImageUri?.let {
                    userResponses["relatedProductImage"] = it.toString()
                }
            }
        }
    }


    private fun updateButtonVisibility() {
        btnPrevious.visibility = if (viewFlipper.displayedChild > 0) View.VISIBLE else View.INVISIBLE
        btnNext.text = if (viewFlipper.displayedChild == viewFlipper.childCount - 1) "완료" else "다음"
    }

    // 이미지 선택 및 업로드 함수
    private fun uploadImage(isRelatedImage: Boolean) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, if (isRelatedImage) REQUEST_CODE_RELATED_IMAGE else REQUEST_CODE_PRODUCT_IMAGE)
    }

    // 이미지 선택 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            selectedImageUri?.let { uri ->
                // 이미지 업로드 로직 구현
                uploadImageToServer(uri, requestCode)
            }
        }
    }

    // 서버에 이미지 업로드
    private fun uploadImageToServer(imageUri: Uri, requestCode: Int) {
        // 서버에 이미지 업로드하는 로직을 여기에 구현합니다.
        // 예: Retrofit을 사용하여 파일을 서버에 업로드합니다.

        // 업로드 후 URL을 저장
        val uploadedImageUrl = "uploaded_image_url_here" // 실제 업로드 후 URL로 교체

        // 업로드한 이미지에 따라 userResponses에 저장
        when (requestCode) {
            REQUEST_CODE_PRODUCT_IMAGE -> {
                userResponses["productImage"] = uploadedImageUrl
            }
            REQUEST_CODE_RELATED_IMAGE -> {
                userResponses["relatedImage"] = uploadedImageUrl
            }
        }
    }


//    private fun initializeChips() {
//        for (i in 0 until chipGroupInterests.childCount) {
//            val chip = chipGroupInterests.getChildAt(i) as Chip
//            // Initialize existing chips with click listeners
//            chip.setOnClickListener {
//                toggleChipSelection(chip)
//            }
//            // Set initial color based on selection state
//            chip.setChipBackgroundColorResource(defaultColor)
//            chip.isChecked = false
//            chip.tag = false
//        }
//    }
//
//    private fun addNewInterest() {
//        val newInterest = etAddInterest.text.toString().trim()
//        if (newInterest.isNotEmpty()) {
//            val newChip = Chip(this).apply {
//                text = newInterest
//                isCloseIconVisible = true
//                setChipBackgroundColorResource(selectedColor) // 선택된 색상으로 변경
//                isClickable = true
//                isCheckable = false
//                tag = true // 선택된 상태로 태그 설정
//                setOnCloseIconClickListener {
//                    chipGroupInterests.removeView(this)
//                }
//                setOnClickListener {
//                    toggleChipSelection(this)
//                }
//            }
//            chipGroupInterests.addView(newChip)
//            etAddInterest.text.clear()
//        } else {
//            Toast.makeText(this, "Please enter an interest", Toast.LENGTH_SHORT).show()
//        }
//    }

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

//    private fun saveUserSettings() {
//        val accessToken = accessToken
//        val nickname = etNickname.text.toString().trim()
//        val interests = getSelectedInterests()
//
//        Log.d("saveUserSettings", "nickname = $nickname, interests = $interests")
//
//        if (nickname.isNotEmpty()) {
//            val userAccount = UserAccount(
//                nickname = nickname,
//                tags = interests
//            )
//            Log.d("saveUserSettings", "userAccount 생성됨 = $userAccount")
//
//            // 서버에 사용자 설정 저장
//            CoroutineScope(Dispatchers.Main).launch {
//                Log.d("saveUserSettings", "서버 요청 시작")
//                try {
//                    val response = withContext(Dispatchers.IO) {
//                        RetrofitInstance.api.updateUserSettings("Bearer $accessToken", userAccount)
//                    }
//                    withContext(Dispatchers.Main) {
//                        if (response.isSuccessful) {
//                            // Save the completed settings status to SharedPreferences
//                            val sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE)
//                            sharedPreferences.edit().apply {
//                                putBoolean("isSettingsCompleted", true)
//                                apply()
//                            }
//
//                            Toast.makeText(this@UserSettingsActivity, "Settings Updated Successfully", Toast.LENGTH_SHORT).show()
//
//                            // Navigate to MainActivity
//                            val intent = Intent(this@UserSettingsActivity, MainActivity::class.java)
//                            startActivity(intent)
//                            finish() // Close the UserSettingsActivity
//                        } else {
//                            Toast.makeText(this@UserSettingsActivity, "Failed to Update Settings: ${response.message()}", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                } catch (e: Exception) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@UserSettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please enter a nickname and ensure you're logged in", Toast.LENGTH_SHORT).show()
//        }
//    }



//    private fun getSelectedInterests(): List<String> {
//        val interests = mutableListOf<String>()
//        for (i in 0 until chipGroupInterests.childCount) {
//            val chip = chipGroupInterests.getChildAt(i) as Chip
//            if (chip.isChecked || chip.tag as? Boolean == true) {
//                interests.add(chip.text.toString())
//            }
//        }
//        return interests
//    }
}