import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import com.example.mappin_fe.R
import kotlinx.coroutines.*

class PlanSelectionFragment : Fragment() {
    private lateinit var billingClient: BillingClient
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val skuDetails = mutableMapOf<String, SkuDetails>()

    // SKU IDs for different plans
    companion object {
        private const val PLAN_990000 = "plan_990000"
        private const val PLAN_29000 = "plan_29000"
        private const val PLAN_6900 = "plan_6900"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan_selection, container, false)
        setupBillingClient()
        setupButtons(view)
        return view
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySkuDetails()
                } else {
                    Toast.makeText(
                        context,
                        "결제 시스템 초기화 실패: ${billingResult.debugMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onBillingServiceDisconnected() {
                // 연결이 끊어졌을 때 재시도 로직
                Toast.makeText(context, "결제 서비스 연결이 끊어졌습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun querySkuDetails() {
        val skuList = listOf(PLAN_990000, PLAN_29000, PLAN_6900)
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.SUBS) // 구독 상품으로 설정
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, detailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                detailsList?.forEach { details ->
                    skuDetails[details.sku] = details
                }
            }
        }
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // 구매 확인 처리
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // 구매 완료 처리 (서버에 구매 정보 전송 등)
//                        updateUserSubscription(purchase.sku)
                    }
                }
            }
        }
    }

    private fun updateUserSubscription(sku: String) {
        // TODO: 서버에 구매 정보 전송
        Toast.makeText(context, "구독이 성공적으로 완료되었습니다!", Toast.LENGTH_LONG).show()
    }

    private fun setupButtons(view: View) {
        view.findViewById<Button>(R.id.btnSelectPlan1).setOnClickListener {
            launchBilling(PLAN_990000)
        }
        view.findViewById<Button>(R.id.btnSelectPlan2).setOnClickListener {
            launchBilling(PLAN_29000)
        }
        view.findViewById<Button>(R.id.btnSelectPlan3).setOnClickListener {
            launchBilling(PLAN_6900)
        }
        view.findViewById<Button>(R.id.btnSelectPlan4).setOnClickListener {
            // 무료 플랜 처리
            Toast.makeText(context, "무료 플랜이 활성화되었습니다!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchBilling(sku: String) {
        skuDetails[sku]?.let { skuDetail ->
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetail)
                .build()
            billingClient.launchBillingFlow(requireActivity(), flowParams)
        } ?: run {
            Toast.makeText(context, "상품 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        billingClient.endConnection()
    }
}