package com.pinAD.pinAD_fe.Profile

import PlanSelectionFragment
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.pinAD.pinAD_fe.Data.user_data.ProfileData
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.Profile.coupon.CouponDialogFragment
import com.pinAD.pinAD_fe.Profile.coupon.CouponScannerFragment
import com.pinAD.pinAD_fe.Profile.notification.NotificationDialogFragment
import com.pinAD.pinAD_fe.network.UserDataManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileFragment : Fragment() {

    private lateinit var imgProfilePicture: ImageView
    private lateinit var tvUserGender: TextView
    private lateinit var tvUserNickname: TextView
    private lateinit var tvPoints: TextView
    private lateinit var btnChargePoints: Button
    private lateinit var btnSettings: Button
    private lateinit var btnCouponBox: Button
    private lateinit var btnQrScan: ImageView
    private lateinit var bellIcon: ImageView
    private lateinit var currentUserUid: String
    private lateinit var billingClient: BillingClient
    private var productDetailsList: List<ProductDetails> = emptyList()
    private lateinit var btnSelectPlan: Button
    private lateinit var inviteButton: Button
    private lateinit var chipGroup: ChipGroup
    private var currentUserData: ProfileData? = null
    private var selectedImagePath: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 뷰 초기화
        imgProfilePicture = view.findViewById(R.id.imgProfilePicture)
        tvUserGender = view.findViewById(R.id.tvUserGender)
        tvUserNickname = view.findViewById(R.id.tvUserNickname)
        tvPoints = view.findViewById(R.id.tvPoints)
        btnChargePoints = view.findViewById(R.id.btnChargePoints)
        btnSettings = view.findViewById(R.id.btnSettings)
        btnCouponBox = view.findViewById(R.id.btnCouponBox)
        btnQrScan = view.findViewById(R.id.btnQrScan)
        btnSelectPlan = view.findViewById(R.id.btnSelectPlan)
        inviteButton = view.findViewById(R.id.btnInviteFriends)
        chipGroup = view.findViewById(R.id.chipGroupInterests)
        bellIcon = view.findViewById(R.id.imgBellIcon)

        inviteButton.setOnClickListener {
            openFriendsInviteFragment()
        }

        btnQrScan.setOnClickListener {
            openCouponScannerFragment()
        }

        btnSelectPlan.setOnClickListener {
//            openWebsite("https://www.example.com")
            navigateToPlanSelectionFragment()
        }

        bellIcon.setOnClickListener {
            NotificationDialogFragment().show(parentFragmentManager, "NotificationDialog")
        }

        btnCouponBox.setOnClickListener {
            showCouponDialog()
        }

        // 설정 버튼 클릭 리스너
        btnSettings.setOnClickListener {
            Log.d("SettingsButton", "Settings button clicked")
            openSettingFragment()
        }

        enableEditing()

        // 프로필 데이터 로드
        loadUserProfile()


        // 포인트 충전 버튼 클릭
        btnChargePoints.setOnClickListener {
            if (!::billingClient.isInitialized || !billingClient.isReady) {
//                Toast.makeText(context, "결제 시스템을 초기화 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
                setupBillingClient()
                return@setOnClickListener
            }
            showPointPackageDialog()
        }

        setupBillingClient()

        return view
    }

    private fun openFriendsInviteFragment() {
        val fragment = FriendsInviteFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openSettingFragment() {
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_body_container, SettingFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun openCouponScannerFragment() {
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_body_container, CouponScannerFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun navigateToPlanSelectionFragment() {
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_body_container, PlanSelectionFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

//    private fun openWebsite(url: String) {
//        val intent = Intent(Intent.ACTION_VIEW)
//        intent.data = Uri.parse(url)
//        startActivity(intent)
//    }

    private fun showCouponDialog() {
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_body_container, CouponDialogFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setupBillingClient() {
        Log.d("BillingClient", "Setting up BillingClient")
        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d("BillingClient", "Setup finished. Response code: ${billingResult.responseCode}")

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // 연결 성공
                    // 필요한 경우 여기서 상품 정보를 미리 로드할 수 있습니다
                    Log.d("BillingClient", "Setup successful. Querying products...")
                    queryAvailableProducts()
                } else {
                    Log.e("BillingClient", "Setup failed: ${billingResult.debugMessage}")
                    // 연결 실패 처리
                    Toast.makeText(
                        context,
                        "결제 시스템 초기화 실패: ${billingResult.debugMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.e("BillingClient", "Billing service disconnected")
                // 연결이 끊어졌을 때 재연결 시도
                retryBillingServiceConnection()
            }
        })
    }

    private fun retryBillingServiceConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // 재연결 성공
                    queryAvailableProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                // 재시도 실패 시 일정 시간 후 다시 시도
                // 실제 구현 시에는 exponential backoff 등을 사용하는 것이 좋습니다
            }
        })
    }

    private fun queryAvailableProducts() {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("product_id_2000") // 2000원 충전 상품
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("product_id_5000") // 5000원 충전 상품
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("product_id_10000") // 1만원 충전 상품
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("product_id_20000") // 2만원 충전 상품
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("product_id_50000") // 5만원 충전 상품
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            // 응답 코드 로깅
            Log.d("BillingClient", "Response Code: ${billingResult.responseCode}")
            Log.d("BillingClient", "Debug Message: ${billingResult.debugMessage}")

            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Log.d("BillingClient", "Products found: ${productDetailsList.size}")
                    this.productDetailsList = productDetailsList

                    // 각 상품 정보 로깅
                    productDetailsList.forEach { product ->
                        Log.d("BillingClient", "Product ID: ${product.productId}")
                        Log.d("BillingClient", "Name: ${product.name}")
                        Log.d("BillingClient", "Description: ${product.description}")
                    }

                    // UI 스레드에서 다이얼로그 표시
                    activity?.runOnUiThread {
                        if (productDetailsList.isNotEmpty()) {
                            showPointPackageDialog()
                        } else {
//                            Toast.makeText(context, "사용 가능한 상품이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                    Log.e("BillingClient", "Service Disconnected")
                    retryBillingServiceConnection()
                }
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                    Log.e("BillingClient", "Service Unavailable")
                    Toast.makeText(context, "결제 서비스를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Log.e("BillingClient", "Query failed with code: ${billingResult.responseCode}")
                    Toast.makeText(context, "상품 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun showPointPackageDialog() {
        if (productDetailsList.isEmpty()) {
//            Toast.makeText(context, "상품 정보를 불러오는 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
            queryAvailableProducts()
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("충전할 포인트 패키지를 선택하세요")

        // 상품 목록을 표시용 문자열로 변환
        val displayOptions = productDetailsList.map { details ->
            val price = details.oneTimePurchaseOfferDetails?.formattedPrice ?: "가격 정보 없음"
            "${getPointAmount(details.productId)}포인트 - $price"
        }.toTypedArray()

        builder.setItems(displayOptions) { dialog, which ->
            val selectedProduct = productDetailsList[which]
            startBillingFlow(selectedProduct)
        }

        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun getPointAmount(productId: String): Int {
        return when (productId) {
            "product_id_2000" -> 600      // 2000원 충전 시 (2000 * 3)
            "product_id_5000" -> 1500     // 5000원 충전 시 (5000 * 3)
            "product_id_10000" -> 3000    // 1만원 충전 시 (10000 * 3)
            "product_id_20000" -> 6000    // 2만원 충전 시 (20000 * 3)
            "product_id_50000" -> 15000   // 5만원 충전 시 (50000 * 3)
            else -> 0
        }
    }

    private fun calculateBonusPoints(productId: String, baseAmount: Int): Int {
        val bonusPercentage = when (productId) {
            "product_id_10000" -> 300  // 1만원 충전 시 10% 보너스
            "product_id_20000" -> 1200  // 2만원 충전 시 20% 보너스
            "product_id_50000" -> 4500 // 5만원 충전 시 30% 보너스
            else -> 0  // 1만원 미만은 보너스 없음
        }
        return (baseAmount * bonusPercentage) / 100
    }


    private fun startBillingFlow(productDetails: ProductDetails) {
        Log.d("BillingTest", "Starting billing flow for ${productDetails.productId}")
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        val billingResult = billingClient.launchBillingFlow(requireActivity(), flowParams)
        Log.d("BillingTest", "Billing flow launch result: ${billingResult.responseCode}")
//        try {
//            val productDetailsParamsList = listOf(
//                BillingFlowParams.ProductDetailsParams.newBuilder()
//                    .setProductDetails(productDetails)
//                    .build()
//            )
//
//            val billingFlowParams = BillingFlowParams.newBuilder()
//                .setProductDetailsParamsList(productDetailsParamsList)
//                .build()
//
//            // 결제 플로우 시작
//            val billingResult = billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
//
//            when (billingResult.responseCode) {
//                BillingClient.BillingResponseCode.OK -> {
//                    // 결제 플로우가 정상적으로 시작됨
//                }
//                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
//                    Toast.makeText(context, "이미 구매한 상품입니다.", Toast.LENGTH_SHORT).show()
//                }
//                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
//                    Toast.makeText(context, "결제 서비스 연결이 끊어졌습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
//                    retryBillingServiceConnection()
//                }
//                else -> {
//                    Toast.makeText(context, "결제를 시작할 수 없습니다. (에러코드: ${billingResult.responseCode})", Toast.LENGTH_SHORT).show()
//                }
//            }
//        } catch (e: Exception) {
//            Toast.makeText(context, "결제 처리 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Toast.makeText(context, "결제가 취소되었습니다.", Toast.LENGTH_SHORT).show()
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Toast.makeText(context, "이미 구매한 상품입니다.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(
                    context,
                    "결제 처리 중 오류가 발생했습니다. (에러코드: ${billingResult.responseCode})",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        Log.d("BillingTest", "Handling purchase: ${purchase.orderId}")
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

//                lifecycleScope.launch {
//                    try {
//                        // 서버에 구매 정보 전송
//                        val response = RetrofitInstance.api.verifyPurchase(
//                            "Bearer $accessToken",
//                            PurchaseInfo(
//                                purchaseToken = purchase.purchaseToken,
//                                productId = purchase.products[0],
//                                orderId = purchase.orderId.toString()
//                            )
//                        )
//
//                        if (response.isSuccessful) {
//                            val productId = purchase.products[0]
//                            val baseAmount = getPointAmount(productId)
//
//                            // 충전된 기본 포인트에 보너스 포인트 계산 후 추가
//                            val bonusPoints = calculateBonusPoints(productId, baseAmount)
//                            val totalPoints = baseAmount + bonusPoints
//
//                            // 서버 검증 성공 후 Google Play에 구매 확인
//                            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
//                                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                                    // 구매 프로세스 완료
//                                    Toast.makeText(context, "포인트 충전이 완료되었습니다. 총 ${totalPoints}포인트가 추가되었습니다.", Toast.LENGTH_SHORT).show()
//                                    loadUserProfile() // 프로필 정보 새로고침
//                                } else {
//                                    Toast.makeText(context, "구매 확인 처리 실패", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                        } else {
//                            Toast.makeText(context, "서버 검증 실패", Toast.LENGTH_SHORT).show()
//                        }
//                    } catch (e: Exception) {
//                        Toast.makeText(context, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Toast.makeText(context, "구매가 완료되었습니다!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            UserDataManager.getUserData()?.let { userAccount ->
                currentUserData = userAccount
                updateUIWithUserData(userAccount)
            } ?: run {
                handleError("프로필 로딩 실패")
            }
        }
    }

    private fun updateUIWithUserData(userAccount: ProfileData) {
        // 메인 스레드에서 UI 업데이트
        activity?.runOnUiThread {
            tvUserGender.text = userAccount.gender ?: "이메일 정보 없음"
            tvUserNickname.text = userAccount.nickname ?: "닉네임 정보 없음"
            tvPoints.text = "${userAccount.points ?: 10000} P"
            chipGroup.removeAllViews()

            userAccount.tags?.let { tags ->
                if (tags.isNotEmpty()) {
                    for (tag in tags) {
                        val chip = Chip(requireContext()).apply {
                            text = tag
                            isClickable = true
                            isCheckable = false
                            setOnClickListener {
                                showTagEditDialog()  // 각 Chip을 클릭했을 때 수정 다이얼로그 표시
                            }
                        }

                        // Chip을 ChipGroup에 추가
                        chipGroup.addView(chip)
                    }
                } else {
                    val noTagsChip = Chip(requireContext()).apply {
                        text = "관심사를 입력해주세요"
                        setOnClickListener {
                            showTagEditDialog()
                        }
                    }
                    chipGroup.addView(noTagsChip)
                }
            }

            // 프로필 이미지가 있는 경우 로드
            userAccount.profile_picture?.let { imageUrl ->
                Glide.with(requireContext())
                    .load(imageUrl)  // URL을 Glide로 로드
                    .into(imgProfilePicture)  // ImageView에 설정
            }
        }
    }

    private fun enableEditing() {
        chipGroup.setOnClickListener(null)
        tvUserNickname.setOnClickListener { showEditDialog("nickname") }
        imgProfilePicture.setOnClickListener { selectImage() }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val selectedImagePath = getRealPathFromURI(uri)
                // 이미지 경로를 이용해 서버에 업로드 처리 추가
                selectedImagePath?.let {
                    updateField("profile_picture", it)
                }
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        context?.contentResolver?.query(uri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex)
            }
        }
        return uri.toString()
    }

    private fun showEditDialog(field: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("수정")
            .setMessage("이 필드를 수정하시겠습니까?")
            .setPositiveButton("수정") { _, _ -> openEditField(field) }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun openEditField(field: String) {
        // 필드에 따라 EditText 열기
        when (field) {
            "nickname" -> showEditTextDialog(tvUserNickname.toString(), "닉네임 수정")
            // 필요한 다른 필드들 추가
        }
    }

    private fun showTagEditDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_edit_tags, null)

        val editText = dialogView.findViewById<EditText>(R.id.editTags)
        val currentTags = currentUserData?.tags?.joinToString(", ") ?: ""
        editText.setText(currentTags)

        builder.setView(dialogView)
            .setTitle("관심사 수정")
            .setPositiveButton("저장") { dialog, _ ->
                val tagsText = editText.text.toString()
                val tagsList = tagsText.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                updateField("tags", tagsList.toString())
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showEditTextDialog(field: String, currentValue: String) {
        val input = EditText(requireContext()).apply {
            setText(currentValue)
            setSingleLine()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("닉네임 수정")
            .setView(input)
            .setPositiveButton("저장") { _, _ ->
                val newValue = input.text.toString()
                if (newValue.isNotBlank()) {
                    updateField(field, newValue)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateField(field: String, newValue: String) {
        val token = RetrofitInstance.getAccessToken()
        currentUserData?.let { userData ->
            val updatedData = when (field) {
                "tags" -> {
                    // 문자열로 받은 태그를 리스트로 변환
                    val tagsList = newValue.removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    userData.copy(tags = tagsList)
                }
                "nickname" -> userData.copy(nickname = newValue)
                "profile_picture" -> userData.copy(profile_picture = newValue)
                else -> userData
            }
            Log.d("UpdateField", "Updated Data: $updatedData")

            lifecycleScope.launch {
                try {
                    val imagePart = if (field == "profile_picture") {
                        val file = File(newValue)
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("image", file.name, requestFile)
                    } else null

                    val userAccountJson = Gson().toJson(updatedData)
                    val userDataBody = RequestBody.create(
                        "application/json".toMediaTypeOrNull(),
                        userAccountJson
                    )

                    val response = RetrofitInstance.api.updateUserProfile("Bearer $token", userDataBody, imagePart)
                    if (response.isSuccessful) {
                        currentUserData = updatedData
                        UserDataManager.updateUserData(updatedData)
                        Toast.makeText(context, "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                        Log.d("UpdateField", "Profile updated successfully")
                    } else {
                        Log.e("UpdateField", "Profile update failed: ${response.message()}")
                        Toast.makeText(context, "업데이트 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("UpdateField", "Network error: ${e.message}")
                    Toast.makeText(context, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleError(errorMessage: String) {
        activity?.runOnUiThread {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}
