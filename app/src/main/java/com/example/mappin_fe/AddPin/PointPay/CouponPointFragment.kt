package com.example.mappin_fe.AddPin.PointPay

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mappin_fe.AddPin.Camera.MediaFile
import com.example.mappin_fe.Data.PinDataResponse
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.MainActivity
import com.example.mappin_fe.R
import com.example.mappin_fe.UserUtils
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
import okio.Buffer
import org.json.JSONObject
import retrofit2.http.Part
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
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private var currentPoints = 1000
    private lateinit var category: String
    private lateinit var info: String
    private lateinit var title: String
    private lateinit var description: String
    private var is_ads: Boolean = false
    private var selectedTags: List<String> = listOf()
    private lateinit var switchVisibility: Switch
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var media_files: List<MediaFile>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var basicConsumption: Int = -5000
    private val basicRange = 100
    private val basicDuration = 60
    private val rangeIncrementCost = 30
    private val durationIncrementCost = 20


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
        info = arguments?.getString("INFO") ?: ""
        title = arguments?.getString("TITLE") ?: ""
        description = arguments?.getString("DESCRIPTION") ?: ""
        is_ads = arguments?.getBoolean("is_Ads", false) ?: false
        selectedTags = arguments?.getStringArray("SELECTED_TAGS")?.toList() ?: emptyList()
        category = arguments?.getString("CATEGORY") ?: ""

        arguments?.let {
            val mediaFilesJson = it.getString("MEDIA_FILES")
            media_files = Gson().fromJson(mediaFilesJson, object : TypeToken<List<MediaFile>>() {}.type)
        }

        Log.d("NewPointSystemFragment", "Number of media files: ${media_files}")
        Log.d("NewPointSystemFragment", "tags: $selectedTags")
        Log.d("NewPointSystemFragment", "Info: $info")
        Log.d("NewPointSystemFragment", "Title: $title")
        Log.d("NewPointSystemFragment", "Description: $description")
        Log.d("NewPointSystemFragment", "is_Ads: $is_ads")

        tvCurrentPoints = view.findViewById(R.id.tv_current_points)
        tvEstimatedCost = view.findViewById(R.id.tv_estimated_cost)
        imgPointIcon = view.findViewById(R.id.img_point_icon)
        spinnerAdRange = view.findViewById(R.id.spinner_ad_range)
        spinnerAdDuration = view.findViewById(R.id.spinner_ad_duration)
        btnCompletePin = view.findViewById(R.id.btn_complete_pin)
        mapView = view.findViewById(R.id.mapView)
        locationSearchEditText = view.findViewById(R.id.locationSearchEditText)
        searchButton = view.findViewById(R.id.searchButton)
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

    private fun updateCurrentPointsText() {
        tvCurrentPoints.text = "Current Points: $currentPoints"
        imgPointIcon.setImageResource(R.drawable.ic_point)
    }

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

    private fun calculateEstimatedCost(): Int {
        val selectedRange = getSelectedRange() - basicRange
        val selectedDuration = getSelectedDuration() - basicDuration

        val rangeCost = if (selectedRange > 0) (selectedRange / 100) * rangeIncrementCost else 0
        val durationCost = if (selectedDuration > 0) (selectedDuration / 10) * durationIncrementCost else 0

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

    private fun createPinData(nickname: String): PinDataResponse {
        val now = Date()
        val location = LatLng(latitude,longitude)
        val infoJson = JSONObject().apply {
            put("range", getSelectedRange())
            put("duration", getSelectedDuration())
            // 기존 info 내용이 있다면 여기에 추가
            put("additionalInfo", info)
        }
        return PinDataResponse(
            id = UUID.randomUUID().toString(),
            latitude = latitude,
            longitude = longitude,
            location = location.toString(),
            user = nickname.toIntOrNull() ?: 0,
            title = title, // title 추가
            description = description, // description 추가
//            category = category,
            media_files = media_files.map { it.uri },
            info = infoJson.toString(),
            tags = selectedTags,
            visibility = if (switchVisibility.isChecked) "public" else "private",
            is_ads = is_ads, // is_ads 추가
            created_at = now,
            updated_at = Date(now.time + (getSelectedDuration() * 60 * 60 * 1000))
        )
    }

    private fun sendPinDataToServer(pinData: PinDataResponse, onComplete: (Boolean) -> Unit) {
        lifecycleScope.launch {
            try {
                val titlePart = pinData.title.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionPart = pinData.description.toRequestBody("text/plain".toMediaTypeOrNull())
                val latitudePart = pinData.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val longitudePart = pinData.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
//                val categoryPart = pinData.category.toRequestBody("text/plain".toMediaTypeOrNull())
                val infoPart = Gson().toJson(pinData.info).toRequestBody("application/json".toMediaTypeOrNull())
                val tagsParts = pinData.tags.map { tag ->
                    tag.toRequestBody("text/plain".toMediaTypeOrNull())
                }.toCollection(ArrayList())
                Log.d("tagspart", "$tagsParts")
                val visibilityPart = pinData.visibility.toRequestBody("text/plain".toMediaTypeOrNull())
                val mediaFileParts = prepareMediaFiles()
                val isAdsPart = (if (pinData.is_ads == true) 1 else 0).toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.savePinDataWithMedia(
                        title = titlePart,
                        description = descriptionPart,
                        latitude = latitudePart,
                        longitude = longitudePart,
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

    private fun getSelectedRange(): Int = when (spinnerAdRange.selectedItemPosition) {
        0 -> 100 // 100m
        1 -> 300 // 300m
        2 -> 500 // 500m
        3 -> 700 // 700m
        4 -> 1000 // 1km
        5 -> 2000 // 2km
        else -> 0 // 잘못된 선택
    }

    private fun getSelectedDuration(): Int = when (spinnerAdDuration.selectedItemPosition) {
        0 -> 60 // 1시간
        1 -> 120 // 2시간
        2 -> 180 // 3시간
        3 -> 240 //4시간
        4 -> 480 // 8시간
        5 -> 960 // 16시간
        6 -> 1440 // 24시간
        else -> 0 // 잘못된 선택
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
