import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.pinAD.pinAD_fe.Data.business.BusinessCreateRequest
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.network.RetrofitInstance
import kotlinx.coroutines.*
import java.io.IOException

class PlanSelectionFragment : Fragment(), OnMapReadyCallback {
    private lateinit var billingClient: BillingClient
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var businessNameEditText: EditText
    private lateinit var businessTypeEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var locationSearchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var submitButton: Button
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var selectedLocation: LatLng? = null
    private var currentLocation: LatLng? = null

    private val skuDetails = mutableMapOf<String, SkuDetails>()

    // SKU IDs for different plans
    companion object {
        private const val PLAN_990000 = "plan_990000"
        private const val PLAN_29000 = "plan_29000"
        private const val PLAN_6900 = "plan_6900"
        private const val REQUEST_LOCATION_PERMISSION = 1
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

    private fun showCreateBusinessDialog(onBusinessInfoEntered: (String, String, String, String, String, Double, Double) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_business, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // EditText와 버튼 연결
        businessNameEditText = dialogView.findViewById(R.id.businessNameEditText)
        businessTypeEditText = dialogView.findViewById(R.id.businessTypeEditText)
        phoneEditText = dialogView.findViewById(R.id.phoneEditText)
        emailEditText = dialogView.findViewById(R.id.emailEditText)
        locationSearchEditText = dialogView.findViewById(R.id.locationSearchEditText)
        searchButton = dialogView.findViewById(R.id.searchButton)
        submitButton = dialogView.findViewById(R.id.submitButton)
        mapView = dialogView.findViewById(R.id.mapView)

        mapView.onCreate(null)

        mapView.getMapAsync(this)

        // 위치 검색 버튼 클릭 처리
        searchButton.setOnClickListener {
            val searchQuery = locationSearchEditText.text.toString()
            if (searchQuery.isNotEmpty()) {
                searchLocation(searchQuery)
            }
        }

        // 비즈니스 등록 버튼 클릭 처리
        submitButton.setOnClickListener {
            val businessName = businessNameEditText.text.toString()
            val businessType = businessTypeEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val email = emailEditText.text.toString()
            val address = locationSearchEditText.text.toString()
            val latitude = selectedLocation?.latitude ?: currentLocation?.latitude ?: 0.0
            val longitude = selectedLocation?.longitude ?: currentLocation?.longitude ?: 0.0

            if (businessName.isNotEmpty() && businessType.isNotEmpty() && address.isNotEmpty()) {
                coroutineScope.launch {
                    createBusiness(businessName, businessType, address, phone, email, latitude, longitude)
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(context, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 다이얼로그 열기
        dialog.show()

        // 사용자의 현재 위치 가져오기
        getUserLocation()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 지도 클릭 시 마커 추가
        googleMap.setOnMapClickListener { latLng ->
            selectedLocation = latLng
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng))
        }

        // 위치 권한이 있다면 MyLocation 활성화
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            getUserLocation() // 사용자 위치 가져오기
        } else {
            // 권한이 없으면 권한 요청
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
            )
        }
    }


    private fun getUserLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        try {
            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                val location = task.result
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                    // 지도에서 현재 위치를 표시
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
                    googleMap.addMarker(MarkerOptions().position(currentLocation!!).title("내 위치"))
                    selectedLocation = currentLocation
                } else {
                    Toast.makeText(context, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "위치 권한 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun createBusiness(
        businessName: String,
        businessType: String,
        address: String,
        phone: String,
        email: String,
        latitude: Double,
        longitude: Double
    ): Boolean {
        try {
            val request = BusinessCreateRequest(
                businessName = businessName,
                businessType = businessType,
                address = address,
                phone = phone,
                email = email,
                latitude = latitude,
                longitude = longitude
            )

            val response = RetrofitInstance.api.createBusiness(request)
            return if (response.isSuccessful) {
                Toast.makeText(context, "비즈니스가 성공적으로 생성되었습니다.", Toast.LENGTH_SHORT).show()
                true
            } else {
                Toast.makeText(context, "비즈니스 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "비즈니스 생성 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    private fun searchLocation(query: String) {
        val geocoder = Geocoder(requireContext())
        try {
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)

                // 지도 이동 및 마커 표시
                googleMap.clear()
                selectedLocation = latLng
                googleMap.addMarker(MarkerOptions().position(latLng).title(query))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            } else {
                Toast.makeText(context, "위치를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(context, "위치 검색 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
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
        when (sku) {
            PLAN_990000, PLAN_29000 -> {
                // 다이얼로그를 띄워서 비즈니스 생성 정보를 입력받기
                showCreateBusinessDialog { businessName, businessType, address, phone, email, latitude, longitude ->
                    // 비즈니스 생성 API 호출 후 결제 진행
                    coroutineScope.launch {
                        val businessCreated = createBusiness(
                            businessName = businessName,
                            businessType = businessType,
                            address = address,
                            phone = phone,
                            email = email,
                            latitude = latitude,
                            longitude = longitude
                        )
                        // 비즈니스 생성 후 결제 진행
                        if (businessCreated) {
                            skuDetails[sku]?.let { skuDetail ->
                                val flowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(skuDetail)
                                    .build()
                                billingClient.launchBillingFlow(requireActivity(), flowParams)
                            } ?: run {
                                Toast.makeText(context, "상품 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "비즈니스 생성에 실패하여 결제를 진행할 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            else -> {
                skuDetails[sku]?.let { skuDetail ->
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetail)
                        .build()
                    billingClient.launchBillingFlow(requireActivity(), flowParams)
                } ?: run {
                    Toast.makeText(context, "상품 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


//    private fun launchBilling(sku: String) {
//        skuDetails[sku]?.let { skuDetail ->
//            val flowParams = BillingFlowParams.newBuilder()
//                .setSkuDetails(skuDetail)
//                .build()
//            billingClient.launchBillingFlow(requireActivity(), flowParams)
//        } ?: run {
//            Toast.makeText(context, "상품 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        billingClient.endConnection()
    }
}