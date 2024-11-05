package com.pinAD.pinAD_fe.AddPin.PointPay

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*

class PointSystemFragment : Fragment(), OnMapReadyCallback {

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
    private var currentPoints = 100000
    private lateinit var category: String
    private lateinit var info: String // contentData 대신 info 사용
    private lateinit var title: String // title 추가
    private lateinit var description: String // description 추가
    private var is_ads: Boolean = false // is_ads 추가
    private var pin_type: Int = 0
    private var selectedTags: List<String> = listOf()
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchVisibility: Switch
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var media_files: List<MediaFile>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var basicConsumption: Int = -3000

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
        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        // 현재 위치를 가져오는 함수 호출
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
                    // 위치를 가져올 수 없을 때의 처리
                }
            }
            .addOnFailureListener { e ->
                Log.e("Location", "Error getting location", e)
                // 위치 가져오기 실패 시 처리
            }
    }


    private fun initializeViews(view: View) {
        info = arguments?.getString("INFO") ?: ""
        title = arguments?.getString("TITLE") ?: "" // title 수신
        description = arguments?.getString("DESCRIPTION") ?: "" // description 수신
        is_ads = arguments?.getBoolean("is_Ads", false) ?: false // is_ads 수신
        selectedTags = arguments?.getStringArray("SELECTED_TAGS")?.toList() ?: emptyList()
        category = arguments?.getString("CATEGORY") ?: ""
        // 미디어 파일 정보 받기
        arguments?.let {
            val mediaFilesJson = it.getString("MEDIA_FILES")
            media_files = Gson().fromJson(mediaFilesJson, object : TypeToken<List<MediaFile>>() {}.type)
        }
        pin_type = arguments?.getInt("PIN_TYPE") ?: 0
        Log.d("PointSystemFragment", "Number of media files: ${media_files}")
        Log.d("PointSystemFragment", "tags: $selectedTags")
        Log.d("PointSystemFragment", "Info: $info")
        Log.d("PointSystemFragment", "Title: $title") // title 로깅
        Log.d("PointSystemFragment", "Description: $description") // description 로깅
        Log.d("PointSystemFragment", "is_Ads: $is_ads") // is_ads 로깅
        Log.d("PointSystemFragment", "pin_type: $pin_type") // is_ads 로깅
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

        // 서울 중심으로 초기 위치 설정
        val seoul = LatLng(37.5665, 126.9780)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15f))

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
    }

    private fun setupSpinners() {
        val rangeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ad_ranges,
            android.R.layout.simple_spinner_item
        )
        rangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAdRange.adapter = rangeAdapter

        val durationAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ad_durations,
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
        tvCurrentPoints.text = "Current Points: $currentPoints"
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
                    if (isAdded && userVisibleHint) { // Fragment가 활성화되어 있는지 확인
                        sendPinDataToServer(pinData) { success ->
                            btnCompletePin.isEnabled = true
                            if (success) {
                                navigateToMainActivity()
                            }
                        }
                    } else {
                        showSafeToast("Fragment is not active")
                        btnCompletePin.isEnabled = true
                    }
                } else {
                    Toast.makeText(context, "Insufficient points!", Toast.LENGTH_SHORT).show()
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
            1 -> 60 // 1시간
            2 -> 180 // 2시간
            3 -> 420 // 4시간
            4 -> 900 // 8시간
            5 -> 1860 // 16시간
            6 -> 2820 // 24시간
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPinData(nickname: String): PinDataResponse {
        val now = Date()
        val location = LatLng(latitude,longitude)
        val range = getSelectedRange()
        val duration = getSelectedDuration() * 3600
        val infoJson = JSONObject().apply {
            // 기존 info 내용이 있다면 여기에 추가
            put("additionalInfo", info)
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
            title = title, // title 추가
            description = description, // description 추가
            pin_type = pin_type,
//            category = category,
            media_files = media_files.map { it.uri },
            info = infoJson.toString(),
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
                Log.d("mediafile", "$mediaFileParts")
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.savePinDataWithMedia(
                        title = titlePart,
                        description = descriptionPart,
                        latitude = latitudePart,
                        longitude = longitudePart,
                        range = rangePart,
                        duration = durationPart,
                        pin_type = pintypePart,
//                        category = categoryPart,
                        media_files = mediaFileParts,
                        info = infoPart,
                        tag_ids = tagsParts,
                        visibility = visibilityPart,
                        is_ads = isAdsPart
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
                val file = File(mediaFile.uri)
                if (!file.exists()) {
                    Log.e("PinData", "File does not exist: ${file.absolutePath}")
                    return@mapNotNull null
                }

                // Determine MIME type based on file extension
                val mimeType = when {
                    file.name.endsWith(".jpg", true) ||
                            file.name.endsWith(".jpeg", true) -> "image/jpeg"
                    file.name.endsWith(".png", true) -> "image/png"
                    file.name.endsWith(".gif", true) -> "image/gif"
                    else -> "application/octet-stream"
                }

                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData(
                    "media_files",
                    file.name,
                    requestFile
                ).also {
                    Log.d("PinData", "Preparing file: ${file.name} (${file.length()} bytes)")
                }
            } catch (e: Exception) {
                Log.e("PinData", "Error preparing media file: ${mediaFile.uri}", e)
                null
            }
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

    private fun showSafeToast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (isAdded && context != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } else {
                Log.w("PinData", "Cannot show toast, fragment is not attached or context is null")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        activity?.finish()
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
        0 -> 0.5 // 30분
        1 -> 1.0 // 1시간
        2 -> 2.0 // 2시간
        3 -> 4.0 //4시간
        4 -> 8.0 // 8시간
        5 -> 16.0 // 16시간
        6 -> 24.0 // 24시간
        else -> 0.0 // 잘못된 선택
    }
}