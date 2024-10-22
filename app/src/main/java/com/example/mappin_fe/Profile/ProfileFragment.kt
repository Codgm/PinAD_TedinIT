package com.example.mappin_fe.Profile

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.example.mappin_fe.Data.CouponResponse
import com.example.mappin_fe.Data.PurchaseInfo
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.R
import com.google.common.collect.ImmutableList
import com.navercorp.nid.oauth.NidOAuthPreferencesManager.accessToken
import kotlinx.coroutines.launch
import retrofit2.Response

class ProfileFragment : Fragment() {

    private lateinit var imgProfilePicture: ImageView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserNickname: TextView
    private lateinit var tvUserDescription: TextView
    private lateinit var tvPoints: TextView
    private lateinit var btnChargePoints: Button
    private lateinit var btnSettings: Button
    private lateinit var tvInterests: TextView
    private lateinit var btnCouponBox: Button
    private lateinit var btnQrScan: Button
    private lateinit var currentUserUid: String
    private lateinit var billingClient: BillingClient
    private var productDetailsList: List<ProductDetails> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 뷰 초기화
        imgProfilePicture = view.findViewById(R.id.imgProfilePicture)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        tvUserNickname = view.findViewById(R.id.tvUserNickname)
        tvUserDescription = view.findViewById(R.id.tvUserDescription)
        tvPoints = view.findViewById(R.id.tvPoints)
        btnChargePoints = view.findViewById(R.id.btnChargePoints)
        btnSettings = view.findViewById(R.id.btnSettings)
        tvInterests = view.findViewById(R.id.tvInterests)
        btnCouponBox = view.findViewById(R.id.btnCouponBox)
        btnQrScan = view.findViewById(R.id.btnQrScan)

        btnQrScan.setOnClickListener {
            openCouponScannerFragment()
        }

        btnCouponBox.setOnClickListener {
            showCouponDialog()
        }


        // Firebase 초기화
//        firebaseAuth = FirebaseAuth.getInstance()
//        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
//        currentUserUid = firebaseAuth.currentUser?.uid ?: ""

        // 프로필 데이터 로드
        loadUserProfile()

        // 설정 버튼 클릭 리스너
        btnSettings.setOnClickListener {
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.main_body_container, SettingFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // 포인트 충전 버튼 클릭
        btnChargePoints.setOnClickListener {
            if (!::billingClient.isInitialized || !billingClient.isReady) {
                Toast.makeText(context, "결제 시스템을 초기화 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
                setupBillingClient()
                return@setOnClickListener
            }
            showPointPackageDialog()
        }

        return view
    }

    private fun openCouponScannerFragment() {
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_body_container, CouponScannerFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun showCouponDialog() {
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_body_container, CouponDialogFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // 연결 성공
                    // 필요한 경우 여기서 상품 정보를 미리 로드할 수 있습니다
                    queryAvailableProducts()
                } else {
                    // 연결 실패 처리
                    Toast.makeText(
                        context,
                        "결제 시스템 초기화 실패: ${billingResult.debugMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onBillingServiceDisconnected() {
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
                        .setProductId("product_id_100")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("product_id_500")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("product_id_1000")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // 상품 정보 저장
                this.productDetailsList = productDetailsList
            }
        }
    }


    private fun showPointPackageDialog() {
        if (productDetailsList.isEmpty()) {
            Toast.makeText(context, "상품 정보를 불러오는 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
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
            "product_id_100" -> 100
            "product_id_500" -> 500
            "product_id_1000" -> 1000
            else -> 0
        }
    }

    private fun startBillingFlow(productDetails: ProductDetails) {
        try {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            // 결제 플로우 시작
            val billingResult = billingClient.launchBillingFlow(requireActivity(), billingFlowParams)

            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    // 결제 플로우가 정상적으로 시작됨
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    Toast.makeText(context, "이미 구매한 상품입니다.", Toast.LENGTH_SHORT).show()
                }
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                    Toast.makeText(context, "결제 서비스 연결이 끊어졌습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    retryBillingServiceConnection()
                }
                else -> {
                    Toast.makeText(context, "결제를 시작할 수 없습니다. (에러코드: ${billingResult.responseCode})", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "결제 처리 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                lifecycleScope.launch {
                    try {
                        // 서버에 구매 정보 전송
                        val response = RetrofitInstance.api.verifyPurchase(
                            "Bearer $accessToken",
                            PurchaseInfo(
                                purchaseToken = purchase.purchaseToken,
                                productId = purchase.products[0],
                                orderId = purchase.orderId.toString()
                            )
                        )

                        if (response.isSuccessful) {
                            // 서버 검증 성공 후 Google Play에 구매 확인
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                    // 구매 프로세스 완료
                                    Toast.makeText(context, "포인트 충전이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                    loadUserProfile() // 프로필 정보 새로고침
                                } else {
                                    Toast.makeText(context, "구매 확인 처리 실패", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "서버 검증 실패", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun loadUserProfile() {
//        databaseReference.child(currentUserUid).addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val userAccount = snapshot.getValue(UserAccount::class.java)
//                userAccount?.let {
//                    tvUserEmail.text = userAccount.emailId ?: "No email provided"
//                    tvUserNickname.text = userAccount.nickname ?: "No nickname provided"
//                    tvUserDescription.text = "Introduce yourself..."  // 사용자 소개는 필요에 따라 변경 가능
//                    tvInterests.text = userAccount.interests ?: "No interests specified"
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Error handling
//                Toast.makeText(context, "Error loading user data: ${error.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
    }
}
