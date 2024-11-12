package com.pinAD.pinAD_fe.AddPin.PointPay

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.pinAD.pinAD_fe.AddPin.Camera.MediaFile
import com.pinAD.pinAD_fe.Data.pin.FTag
import com.pinAD.pinAD_fe.Data.pin.PinDataResponse
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.MainActivity
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.UserUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pinAD.pinAD_fe.AddPin.Review.ReviewLocationFragment
import com.pinAD.pinAD_fe.AddPin.Review.ReviewLocationFragment.Companion
import com.pinAD.pinAD_fe.Profile.ProfileFragment
import com.pinAD.pinAD_fe.network.UserDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*

class CouponPointFragment : Fragment(), OnMapReadyCallback {

    private lateinit var tvCurrentPoints: TextView
    private lateinit var tvEstimatedCost: TextView
    private lateinit var imgPointIcon: ImageView
    private lateinit var btnCompletePin: Button
    private lateinit var spinnerAdRange: Spinner
    private lateinit var spinnerAdDuration: Spinner
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var locationSearchEditText: EditText
    private lateinit var searchButton: Button
    private var selectedLocation: LatLng? = null
    private var currentPoints: Int = 0
    private lateinit var category: String
    private var is_ads: Boolean = false
    private var selectedTags: List<String> = listOf()
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchVisibility: Switch
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var media_files: List<MediaFile>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var basicConsumption: Int = -5000
    private var pin_type: Int = 0
    private var product_name: String = ""
    private var discount_amount: String = ""
    private var discountInfo: JSONObject? = null

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_point_system, container, false)
        initializeViews(view)
        setupSpinners()
        updateCurrentPointsText()
        setupListeners()
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getCurrentLocation()
        return view
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                    Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
                } ?: run {
                    Log.e("Location", "Unable to get current location")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Location", "Error getting location", e)
            }
    }

    private fun initializeViews(view: View) {
        discountInfo = try {
            arguments?.getString("INFO", "{}")?.let { JSONObject(it) }
        } catch (e: Exception) {
            JSONObject()
        }
        product_name = arguments?.getString("TITLE") ?: ""
        discount_amount = arguments?.getString("DESCRIPTION") ?: ""
        is_ads = arguments?.getBoolean("is_Ads", false) ?: false
        selectedTags = arguments?.getStringArray("SELECTED_TAGS")?.toList() ?: emptyList()
        category = arguments?.getString("CATEGORY") ?: ""

        arguments?.let {
            val mediaFilesJson = it.getString("MEDIA_FILES")
            media_files = Gson().fromJson(mediaFilesJson, object : TypeToken<List<MediaFile>>() {}.type)
        }
        pin_type = arguments?.getInt("PIN_TYPE") ?: 0

        Log.d("NewPointSystemFragment", "Number of media files: ${media_files}")
        Log.d("NewPointSystemFragment", "tags: $selectedTags")
        Log.d("NewPointSystemFragment", "Info: $discountInfo")
        Log.d("NewPointSystemFragment", "Title: $product_name")
        Log.d("NewPointSystemFragment", "Description: $discount_amount")
        Log.d("NewPointSystemFragment", "is_Ads: $is_ads")
        Log.d("PointSystemFragment", "pin_type: $pin_type")
        tvCurrentPoints = view.findViewById(R.id.tv_current_points)
        tvEstimatedCost = view.findViewById(R.id.tv_estimated_cost)
        imgPointIcon = view.findViewById(R.id.img_point_icon)
        spinnerAdRange = view.findViewById(R.id.spinner_ad_range)
        spinnerAdDuration = view.findViewById(R.id.spinner_ad_duration)
        btnCompletePin = view.findViewById(R.id.btn_complete_pin)
        mapView = view.findViewById(R.id.mapView)
        locationSearchEditText = view.findViewById(R.id.locationSearchEditText)
        searchButton = view.findViewById(R.id.searchButton)
        switchVisibility = view.findViewById(R.id.switchVisibility)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 지도 클릭 리스너 설정
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            selectedLocation = latLng
            latitude = latLng.latitude
            longitude = latLng.longitude
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("선택한 위치")
            )
        }

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
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                    googleMap.addMarker(MarkerOptions().position(userLatLng).title("내 위치"))
                } else {
                    Toast.makeText(context, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "위치 권한 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 허용된 경우
                    getUserLocation()
                } else {
                    // 권한이 거부된 경우
                    Toast.makeText(context, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSpinners() {
        val rangeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.coupon_ranges,
            android.R.layout.simple_spinner_item
        )
        rangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAdRange.adapter = rangeAdapter

        val durationAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.coupon_durations,
            android.R.layout.simple_spinner_item
        )
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAdDuration.adapter = durationAdapter
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
                latitude = latLng.latitude
                longitude = latLng.longitude
                googleMap.addMarker(MarkerOptions().position(latLng).title(query))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            } else {
                Toast.makeText(context, "위치를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(context, "위치 검색 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrentPointsText() {
        currentPoints = UserDataManager.userData?.points ?: 100000
        tvCurrentPoints.text = "보유 포인트: ${currentPoints}"
        imgPointIcon.setImageResource(R.drawable.ic_point)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        spinnerAdRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                calculateEstimatedCost()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerAdDuration.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                calculateEstimatedCost()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        searchButton.setOnClickListener {
            val searchQuery = locationSearchEditText.text.toString()
            if (searchQuery.isNotEmpty()) {
                searchLocation(searchQuery)
            }
        }

        btnCompletePin.setOnClickListener {
            btnCompletePin.isEnabled = false
            UserUtils.fetchUserDetails { nickname, _ ->
                if (calculateAndUpdatePoints()) {
                    val pinData = createPinData(nickname)
                    if (isAdded && userVisibleHint) {
                        sendPinDataToServer(pinData) { success ->
                            btnCompletePin.isEnabled = true
                            if (success) {
                                navigateToMainActivity()
                                updatePointsAndFetchLatestData()
                            }
                        }
                    } else {
                        showSafeToast("Fragment is not active")
                        btnCompletePin.isEnabled = true
                    }
                } else {
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("포인트 부족")
                        .setMessage("보유하신 포인트가 부족합니다. 포인트를 충전하세요.")
                        .setPositiveButton("포인트 결제") { _, _ ->
                            parentFragmentManager.beginTransaction().apply {
                                replace(R.id.fragment_container, ProfileFragment())
                                addToBackStack(null)
                                commit()
                            }
                        }
                        .setNegativeButton("취소", null)
                        .create()
                    dialog.show()
                    btnCompletePin.isEnabled = true
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun calculateEstimatedCost(): Int {
        val rangeCost = when (spinnerAdRange.selectedItemPosition) {
            1 -> 90
            2 -> 150
            3 -> 210
            4 -> 300
            5 -> 600
            else -> 0
        }

        val durationCost = when (spinnerAdDuration.selectedItemPosition) {
            1 -> 120 // 2시간
            2 -> 240 // 3시간
            3 -> 360 // 4시간
            4 -> 840 // 8시간
            5 -> 1800 // 16시간
            6 -> 2760 // 24시간
            else -> 0 // 기본값
        }

        val totalCost = rangeCost + durationCost - basicConsumption
        tvEstimatedCost.text = "Estimated Cost: $totalCost"
        return totalCost
    }

    private fun calculateAndUpdatePoints(): Boolean {
        val totalCost = calculateEstimatedCost()
        return if (currentPoints >= totalCost) {
            currentPoints -= totalCost
            updateCurrentPointsText()
            true
        } else {
            false
        }
    }

    private fun updatePointsAndFetchLatestData() {
        lifecycleScope.launch {
            val updatedProfileData = UserDataManager.getUserData(forceRefresh = true)
            updatedProfileData?.let {
                // UI 갱신
                currentPoints = it.points!!
                updateCurrentPointsText()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPinData(nickname: String): PinDataResponse {
        val now = Date()
        val location = LatLng(latitude,longitude)
        val range = getSelectedRange()
        val duration = getSelectedDuration()
        val infoJson = JSONObject().apply {
            // 기존 info 내용이 있다면 여기에 추가
            put("additionalInfo", discountInfo)
        }
        val tags = selectedTags.map { FTag(it) }

        return PinDataResponse(
            id = UUID.randomUUID().toString(),
            latitude = latitude,
            longitude = longitude,
            location = location.toString(),
            range = range,
            duration = duration.toString(),
            user = nickname.toIntOrNull() ?: 0,
            title = product_name, // title 추가
            description = discount_amount, // description 추가
            pin_type = pin_type,
//            category = category,
            media_files = media_files.map { it.uri },
//            info = infoJson.toString(),
            info = discountInfo,
            tags = tags,
            visibility = if (switchVisibility.isChecked) "public" else "private",
            is_ads = is_ads, // is_ads 추가
            created_at = now,
            updated_at = Date(now.time)
        )
    }

    private fun sendPinDataToServer(pinData: PinDataResponse, onComplete: (Boolean) -> Unit) {
        lifecycleScope.launch {
            try {
                val titlePart = pinData.title.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionPart = pinData.description.toRequestBody("text/plain".toMediaTypeOrNull())
                val latitudePart = pinData.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val longitudePart = pinData.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val rangePart = pinData.range.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val durationPart = pinData.duration.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val pintypePart = pinData.pin_type.toString().toRequestBody("text/plain".toMediaTypeOrNull())
//                val categoryPart = pinData.category.toRequestBody("text/plain".toMediaTypeOrNull())
                val infoPart = Gson().toJson(pinData.info).toRequestBody("application/json".toMediaTypeOrNull())
                val tagsParts = ArrayList<RequestBody>()
                // Tag 객체에서 name 필드를 사용하여 RequestBody로 변환
                pinData.tags.forEach { tag ->
                    tagsParts.add(tag.name.toRequestBody("text/plain".toMediaTypeOrNull())) // 수정된 부분
                }
                Log.d("tagspart", "$tagsParts")
                val visibilityPart = pinData.visibility.toRequestBody("text/plain".toMediaTypeOrNull())
                val mediaFileParts = prepareMediaFiles()
                val isAdsPart = (if (pinData.is_ads == true) 1 else 0).toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val discountType = if (discountInfo != null && discountInfo!!.has("discount_type")) {
                    discountInfo!!.getString("discount_type")
                } else {
                    "PERCENTAGE" // 기본값
                }
                val maxissuedcount = if (discountInfo != null && discountInfo!!.has("max_issued_count")) {
                    discountInfo!!.getInt("max_issued_count")
                } else {
                    0 // 기본값
                }
                val discountTypePart = discountType.toRequestBody("text/plain".toMediaTypeOrNull())
                val maxissuedcountPart = maxissuedcount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val discountAmountFloat = try {
                    discount_amount.toFloat()
                } catch (e: NumberFormatException) {
                    Log.e("CouponPin", "Invalid discount amount format: $discount_amount")
                    showSafeToast("할인 금액 형식이 올바르지 않습니다")
                    onComplete(false)
                    return@launch
                }
                val discountAmountPart = discountAmountFloat.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                Log.d("CouponPin", """
                Sending data to server:
                - Product Name: ${pinData.title}
                - Discount Amount: $discountAmountFloat
                - Discount Type: $discountType
                - Latitude: ${pinData.latitude}
                - Longitude: ${pinData.longitude}
            """.trimIndent())

                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.saveCouponPinWithMedia(
                        title = titlePart,
//                        description = descriptionPart,
                        discount_type = discountTypePart,
                        discount_amount = discountAmountPart,
                        latitude = latitudePart,
                        longitude = longitudePart,
                        max_num = maxissuedcountPart,
//                        range = rangePart,
//                        duration = durationPart,
//                        pin_type = pintypePart,
////                        category = categoryPart,
//                        media_files = mediaFileParts,
//                        info = infoPart,
//                        tag_ids = tagsParts,
//                        visibility = visibilityPart,
//                        is_ads = isAdsPart
                    )
                }

                if (response.isSuccessful) {
                    Log.d("PinData", "Pin data and media saved successfully: ${response.body()}")
                    onComplete(true)
                } else {
                    Log.e("PinData", "Error saving pin data and media: HTTP ${response.code()} - ${response.errorBody()?.string()}")
                    showSafeToast("Error saving pin data and media: ${response.code()}")
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e("PinData", "Exception saving pin data and media", e)
                showSafeToast("Exception saving pin data and media: ${e.message}")
                onComplete(false)
            }
        }
    }

    private suspend fun prepareMediaFiles(): List<MultipartBody.Part> = withContext(Dispatchers.IO) {
        media_files.mapNotNull { mediaFile ->
            try {
                val filePath = mediaFile.uri // 여기에서 mediaFile.uri가 실제 파일 경로라고 가정
                val file = File(filePath)
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull()) // 이미지 MIME 타입 설정
                MultipartBody.Part.createFormData("media_files", file.name, requestFile)
            } catch (e: Exception) {
                Log.e("PinData", "Error preparing media file: ${mediaFile.uri}", e)
                null
            }
        }
    }

    private fun getSelectedRange(): Double = when (spinnerAdRange.selectedItemPosition) {
        0 -> 0.1 // 100m
        1 -> 0.3 // 300m
        2 -> 0.5 // 500m
        3 -> 0.7 // 700m
        4 -> 1.0 // 1km
        5 -> 2.0 // 2km
        else -> 0.0 // 잘못된 선택
    }

    private fun getSelectedDuration(): Double = when (spinnerAdDuration.selectedItemPosition) {
        0 -> 1.0 // 1시간
        1 -> 2.0 // 2시간
        2 -> 3.0 // 3시간
        3 -> 4.0 //4시간
        4 -> 8.0 // 8시간
        5 -> 16.0 // 16시간
        6 -> 24.0 // 24시간
        else -> 0.0 // 잘못된 선택
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(context, MainActivity::class.java))
        activity?.finish()
    }

    private fun showSafeToast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // MapView 생명주기 메서드들
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
