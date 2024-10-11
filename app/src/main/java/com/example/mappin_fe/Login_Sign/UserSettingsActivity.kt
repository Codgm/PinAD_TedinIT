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
import android.widget.ImageView
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
import androidx.lifecycle.lifecycleScope
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.Data.UserAccount
import com.example.mappin_fe.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserSettingsActivity : AppCompatActivity() {
    private var accessToken: String? = null

//    private lateinit var etNickname: EditText
    private lateinit var chipGroupInterests: ChipGroup
    private lateinit var etAddInterest: EditText
//    private lateinit var btnAddInterest: Button
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var btnNext: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnUploadProfilePhoto: Button
    private lateinit var btnUploadProductImage: Button
    private lateinit var btnUploadRelatedImage: Button
    private lateinit var ivProfilePhoto: ImageView
    private var profilePhotoUri: Uri? = null
    private val userResponses = mutableMapOf<String, Any>()
    private lateinit var imageViewProfilePhoto: ImageView
    private lateinit var imageViewRelatedImage: ImageView
    private lateinit var imageViewProductImage: ImageView


    companion object {
        private const val REQUEST_CODE_PRODUCT_IMAGE = 1001
        private const val REQUEST_CODE_RELATED_IMAGE = 1002
        private const val REQUEST_CODE_PROFILE_PHOTO = 1003
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
        btnUploadProfilePhoto = findViewById(R.id.btnUploadProfilePhoto)
        btnUploadProductImage = findViewById(R.id.btnUploadInterestImage)
        btnUploadRelatedImage = findViewById(R.id.btnUploadRelatedImage)
        imageViewProfilePhoto = findViewById(R.id.imageViewProfilePhoto) // 레이아웃에서 해당 ID로 ImageView를 찾기
        imageViewProductImage = findViewById(R.id.imageViewInterestProduct)
        imageViewRelatedImage = findViewById(R.id.imageViewRelatedProduct)

        initializeViews()

        btnUploadProfilePhoto.setOnClickListener {
            uploadProfilePhoto()
        }
        btnUploadProductImage.setOnClickListener {
            uploadProductImage()
        }
        btnUploadRelatedImage.setOnClickListener {
            uploadRelatedImage()
        }

        btnNext.setOnClickListener {
            if (viewFlipper.displayedChild < viewFlipper.childCount - 1) {
                saveCurrentPageResponse()
                viewFlipper.showNext()
                updateButtonVisibility()
            } else {
                saveUserSettings()
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

        // 3. 연령대 선택
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
        val etAddShoppingInterest = findViewById<EditText>(R.id.etAddShoppingInterest)
        val btnAddShoppingInterest = findViewById<Button>(R.id.btnAddShoppingInterest)

        btnAddShoppingInterest.setOnClickListener {
            val newInterest = etAddShoppingInterest.text.toString()
            if (newInterest.isNotEmpty()) {
                addChipToGroup(shoppingInterestsChipGroup, newInterest)
                etAddShoppingInterest.text.clear() // Clear the input field after adding
            }
        }

        shoppingInterestsChipGroup.setOnCheckedChangeListener { group, _ ->
            val interests = group.checkedChipIds.map { id ->
                (group.findViewById<Chip>(id)).text.toString()
            }
            userResponses["shoppingInterests"] = interests
        }

        // 4. 주로 쇼핑하는 지역
        val shoppingAreasChipGroup = findViewById<ChipGroup>(R.id.chipGroupShoppingAreas)
        val etAddShoppingArea = findViewById<EditText>(R.id.etAddShoppingArea)
        val btnAddShoppingArea = findViewById<Button>(R.id.btnAddShoppingArea)

        btnAddShoppingArea.setOnClickListener {
            val newArea = etAddShoppingArea.text.toString()
            if (newArea.isNotEmpty()) {
                addChipToGroup(shoppingAreasChipGroup, newArea)
                etAddShoppingArea.text.clear()
            }
        }

        shoppingAreasChipGroup.setOnCheckedChangeListener { group, _ ->
            val areas = group.checkedChipIds.map { id ->
                (group.findViewById<Chip>(id)).text.toString()
            }
            userResponses["shoppingAreas"] = areas
        }

        // 5. 선호하는 브랜드
        val brandPreferencesChipGroup = findViewById<ChipGroup>(R.id.chipGroupPreferredBrands)
        val etAddBrand = findViewById<EditText>(R.id.etAddBrand)
        val btnAddBrand = findViewById<Button>(R.id.btnAddBrand)

        btnAddBrand.setOnClickListener {
            val newBrand = etAddBrand.text.toString()
            if (newBrand.isNotEmpty()) {
                addChipToGroup(brandPreferencesChipGroup, newBrand)
                etAddBrand.text.clear()
            }
        }

        brandPreferencesChipGroup.setOnCheckedChangeListener { group, _ ->
            val brands = group.checkedChipIds.map { id ->
                (group.findViewById<Chip>(id)).text.toString()
            }
            userResponses["brandPreferences"] = brands
        }

        // 6. 쇼핑 시 중요 요소
        val shoppingPrioritiesChipGroup = findViewById<ChipGroup>(R.id.chipGroupImportance)
        val etAddPriority = findViewById<EditText>(R.id.etAddPriority)
        val btnAddPriority = findViewById<Button>(R.id.btnAddPriority)

        btnAddPriority.setOnClickListener {
            val newPriority = etAddPriority.text.toString()
            if (newPriority.isNotEmpty()) {
                addChipToGroup(shoppingPrioritiesChipGroup, newPriority)
                etAddPriority.text.clear()
            }
        }

        shoppingPrioritiesChipGroup.setOnCheckedChangeListener { group, _ ->
            val priorities = group.checkedChipIds.map { id ->
                (group.findViewById<Chip>(id)).text.toString()
            }
            userResponses["shoppingPriorities"] = priorities
        }

        // 7. 관심사 및 취미
        val hobbiesInterestsChipGroup = findViewById<ChipGroup>(R.id.chipGroupHobbiesInterests)
        val etAddHobby = findViewById<EditText>(R.id.etAddHobby)
        val btnAddHobby = findViewById<Button>(R.id.btnAddHobby)

        btnAddHobby.setOnClickListener {
            val newHobby = etAddHobby.text.toString()
            if (newHobby.isNotEmpty()) {
                addChipToGroup(hobbiesInterestsChipGroup, newHobby)
                etAddHobby.text.clear()
            }
        }

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
//        val btnUploadProductImage = findViewById<Button>(R.id.btnUploadInterestImage)
//        btnUploadProductImage.setOnClickListener {
//            // 관심 제품 이미지를 업로드합니다.
//            uploadImage(REQUEST_CODE_PRODUCT_IMAGE)
//        }
//
//        // 13. 관련 제품 이미지 업로드
//        val btnUploadRelatedImage = findViewById<Button>(R.id.btnUploadRelatedImage)
//        btnUploadRelatedImage.setOnClickListener {
//            // 관련 제품 이미지를 업로드합니다.
//            uploadImage(REQUEST_CODE_RELATED_IMAGE)
//        }
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
                // 프로필 사진 저장 (이미 uploadImageToServer에서 처리됨)
            }
            4 -> {
                // 관심 쇼핑 영역 저장
                val selectedInterests = findViewById<ChipGroup>(R.id.chipGroupShoppingInterests).checkedChipIds.map { id ->
                    findViewById<Chip>(id).text.toString()
                }
                if (selectedInterests.isNotEmpty()) {
                    userResponses["shoppingInterests"] = selectedInterests
                }
            }
            5 -> {
                // 쇼핑 지역 저장
                val selectedAreas = findViewById<ChipGroup>(R.id.chipGroupShoppingAreas).checkedChipIds.map { id ->
                    findViewById<Chip>(id).text.toString()
                }
                if (selectedAreas.isNotEmpty()) {
                    userResponses["shoppingAreas"] = selectedAreas
                }
            }
            6 -> {
                // 선호 브랜드 저장
                val selectedBrands = findViewById<ChipGroup>(R.id.chipGroupPreferredBrands).checkedChipIds.map { id ->
                    findViewById<Chip>(id).text.toString()
                }
                if (selectedBrands.isNotEmpty()) {
                    userResponses["brandPreferences"] = selectedBrands
                }
            }
            7 -> {
                // 쇼핑 우선순위 저장
                val selectedPriorities = findViewById<ChipGroup>(R.id.chipGroupImportance).checkedChipIds.map { id ->
                    findViewById<Chip>(id).text.toString()
                }
                if (selectedPriorities.isNotEmpty()) {
                    userResponses["shoppingPriorities"] = selectedPriorities
                }
            }
            8 -> {
                // 취미와 관심사 저장
                val selectedHobbies = findViewById<ChipGroup>(R.id.chipGroupHobbiesInterests).checkedChipIds.map { id ->
                    findViewById<Chip>(id).text.toString()
                }
                if (selectedHobbies.isNotEmpty()) {
                    userResponses["hobbiesInterests"] = selectedHobbies
                }
            }
            9 -> {
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
            10 -> {
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
            11 -> {
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
            12 -> {
                // 쿠폰/선물 입력 저장
                val couponGift = findViewById<EditText>(R.id.etCouponGift).text.toString().trim()
                if (couponGift.isNotEmpty()) {
                    userResponses["couponGift"] = couponGift
                }
            }
            13 -> {
//                // 관심 제품 이미지 업로드 저장
//                val interestProductImageUri = findViewById<Button>(R.id.btnUploadInterestImage) // 이미지 URI 저장
//                interestProductImageUri?.let {
//                    userResponses["interestProductImage"] = it.toString()
//                }
            }
            14 -> {
//                // 관련 제품 이미지 업로드 저장
//                val relatedProductImageUri = findViewById<Button>(R.id.btnUploadRelatedImage) // 이미지 URI 저장
//                relatedProductImageUri?.let {
//                    userResponses["relatedProductImage"] = it.toString()
//                }
            }
        }
        Log.d("UserResponsesData", "Current userResponses: $userResponses")
    }

    private fun addChipToGroup(chipGroup: ChipGroup, label: String) {
        val chip = Chip(this)
        chip.text = label
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip)
        }
        chipGroup.addView(chip)
    }


    private fun updateButtonVisibility() {
        btnPrevious.visibility = if (viewFlipper.displayedChild > 0) View.VISIBLE else View.INVISIBLE
        btnNext.text = if (viewFlipper.displayedChild == viewFlipper.childCount - 1) "완료" else "다음"
    }

    // 이미지 선택 및 업로드 함수
    private fun uploadImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    // 호출
    private fun uploadProfilePhoto() {
        uploadImage(REQUEST_CODE_PROFILE_PHOTO)
    }

    private fun uploadRelatedImage() {
        uploadImage(REQUEST_CODE_RELATED_IMAGE)
    }

    private fun uploadProductImage() {
        uploadImage(REQUEST_CODE_PRODUCT_IMAGE)
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

    private fun uploadImageToServer(imageUri: Uri, requestCode: Int) {
        // 이미지 업로드 로직 구현

        // 업로드 후 URL을 저장
        val uploadedImageUrl = "uploaded_image_url_here" // 실제 업로드 후 URL로 교체

        val realImagePath = getRealPathFromURI(imageUri)

        when (requestCode) {
            REQUEST_CODE_PROFILE_PHOTO -> {
                userResponses["profilePhoto"] = uploadedImageUrl // 업로드한 이미지의 URL
                // 실제 경로도 저장 (선택 사항)
                userResponses["profilePhotoRealPath"] = realImagePath ?: "unknown"
                imageViewProfilePhoto.setImageURI(imageUri) // 미리보기
                Log.d("UploadImage", "Profile photo uploaded: $uploadedImageUrl")
                Log.d("UploadImage", "Profile photo real path: $realImagePath")
            }

            REQUEST_CODE_RELATED_IMAGE -> {
                userResponses["relatedImage"] = uploadedImageUrl
                userResponses["relatedImageRealPath"] = realImagePath ?: "unknown"
                imageViewRelatedImage.setImageURI(imageUri) // 미리보기
                Log.d("UploadImage", "Related image uploaded: $uploadedImageUrl")
                Log.d("UploadImage", "Related image real path: $realImagePath")
            }

            REQUEST_CODE_PRODUCT_IMAGE -> {
                userResponses["productImage"] = uploadedImageUrl
                userResponses["productImageRealPath"] = realImagePath ?: "unknown"
                imageViewProductImage.setImageURI(imageUri) // 미리보기
                Log.d("UploadImage", "Product image uploaded: $uploadedImageUrl")
                Log.d("UploadImage", "Product image real path: $realImagePath")
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
        }
        return path
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
        val token = accessToken ?: run {
            Toast.makeText(this, "Access token is missing", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            try {
                val userAccount = createUserAccountFromResponses()
                Log.d("userAccount", "$userAccount")
                val response = RetrofitInstance.api.updateUserSettings("Bearer $token", userAccount)

                if (response.isSuccessful) {
                    saveSettingsCompletionStatus()
                    navigateToMainActivity()
                } else {
                    Toast.makeText(
                        this@UserSettingsActivity,
                        "Failed to update settings: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UserSettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createUserAccountFromResponses(): UserAccount {
        Log.d("UserAccountData", "Creating UserAccount from responses")
        userResponses.forEach { (key, value) ->
            Log.d("UserAccountData", "Key: $key, Value: $value")
        }

        return UserAccount(
            nickname = userResponses["nickname"] as? String,
            gender = userResponses["gender"] as? String,
            age = userResponses["age"] as? String,
            profilePhoto = userResponses["profilePhoto"] as? String,
            shoppingInterests = userResponses["shoppingInterests"] as? List<String>,
            shoppingAreas = userResponses["shoppingAreas"] as? List<String>,
            brandPreferences = userResponses["brandPreferences"] as? List<String>,
            shoppingPriorities = userResponses["shoppingPriorities"] as? List<String>,
            hobbiesInterests = userResponses["hobbiesInterests"] as? List<String>,
            preferredShoppingTime = userResponses["preferredShoppingTime"] as? String,
            notificationRadius = userResponses["notificationRadius"] as? String,
            maxPushNotifications = userResponses["maxPushNotifications"] as? String,
            couponGift = userResponses["couponGift"] as? String,
            productImage = userResponses["productImage"] as? String,
            relatedImage = userResponses["relatedImage"] as? String
        )
    }

    private fun saveSettingsCompletionStatus() {
        getSharedPreferences("UserSettings", MODE_PRIVATE).edit().apply {
            putBoolean("isSettingsCompleted", true)
            apply()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this@UserSettingsActivity, MainActivity::class.java))
        finish()
    }
}