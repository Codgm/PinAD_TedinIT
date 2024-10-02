package com.example.mappin_fe.AddPin.PointPay

import android.annotation.SuppressLint
import android.content.Intent
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
import com.google.android.gms.maps.model.LatLng
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
import java.util.*

class PointSystemFragment : Fragment() {

    private lateinit var tvCurrentPoints: TextView
    private lateinit var tvEstimatedCost: TextView
    private lateinit var imgPointIcon: ImageView
    private lateinit var btnCompletePin: Button
    private lateinit var spinnerAdRange: Spinner
    private lateinit var spinnerAdDuration: Spinner
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchAdBoost: Switch
    private var currentPoints = 1000
    private lateinit var category: String
    private lateinit var info: String // contentData 대신 info 사용
    private lateinit var title: String // title 추가
    private lateinit var description: String // description 추가
    private var is_ads: Boolean = false // is_ads 추가
    private var selectedTags: List<String> = listOf()
    private lateinit var switchVisibility: Switch
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var media_files: List<MediaFile>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_point_system, container, false)
        initializeViews(view)
        setupSpinners()
        updateCurrentPointsText()
        setupListeners()
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
        Log.d("PointSystemFragment", "Number of media files: ${media_files}")
        Log.d("PointSystemFragment", "tags: $selectedTags")
        Log.d("PointSystemFragment", "Info: $info")
        Log.d("PointSystemFragment", "Title: $title") // title 로깅
        Log.d("PointSystemFragment", "Description: $description") // description 로깅
        Log.d("PointSystemFragment", "is_Ads: $is_ads") // is_ads 로깅
        tvCurrentPoints = view.findViewById(R.id.tv_current_points)
        tvEstimatedCost = view.findViewById(R.id.tv_estimated_cost)
        imgPointIcon = view.findViewById(R.id.img_point_icon)
        spinnerAdRange = view.findViewById(R.id.spinner_ad_range)
        spinnerAdDuration = view.findViewById(R.id.spinner_ad_duration)
        switchAdBoost = view.findViewById(R.id.switch_ad_boost)
        btnCompletePin = view.findViewById(R.id.btn_complete_pin)
        switchVisibility = view.findViewById(R.id.switch_visibility)
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

        switchAdBoost.setOnCheckedChangeListener { _, _ ->
            calculateEstimatedCost()
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

    private fun calculateEstimatedCost(): Int {
        val rangeCost = when (spinnerAdRange.selectedItemPosition) {
            1 -> 50
            2 -> 100
            else -> 0
        }

        val durationCost = when (spinnerAdDuration.selectedItemPosition) {
            1 -> 100
            2 -> 200
            else -> 0
        }

        val boostCost = if (switchAdBoost.isChecked) 50 else 0

        val totalCost = rangeCost + durationCost + boostCost
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
            category = category,
            media_files = media_files.map { it.uri },
            info = infoJson.toString(),
            tags = selectedTags,
            visibility = if (switchVisibility.isChecked) "public" else "private",
            is_ads = is_ads, // is_ads 추가
            created_at = now,
            updated_at = Date(now.time + (getSelectedDuration() * 60 * 60 * 1000))
        )
    }


    fun requestBodyToString(requestBody: RequestBody): String {
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        return buffer.readUtf8()
    }


    private fun sendPinDataToServer(pinData: PinDataResponse, onComplete: (Boolean) -> Unit) {
        lifecycleScope.launch {
            try {
                val titlePart = pinData.title.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionPart = pinData.description.toRequestBody("text/plain".toMediaTypeOrNull())
                val latitudePart = pinData.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val longitudePart = pinData.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryPart = pinData.category.toRequestBody("text/plain".toMediaTypeOrNull())
                val infoPart = Gson().toJson(pinData.info).toRequestBody("application/json".toMediaTypeOrNull())
                val tagsParts = pinData.tags.map { tag ->
                    tag.toRequestBody("text/plain".toMediaTypeOrNull())
                }as ArrayList<RequestBody>
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
                        category = categoryPart,
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

    private fun showSafeToast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (isAdded && context != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } else {
                Log.w("PinData", "Cannot show toast, fragment is not attached or context is null")
            }
        }
    }


    private fun preparePinDataPart(pinData: PinDataResponse): RequestBody {
        val gson = Gson()
        val pinDataJson = gson.toJson(pinData)
        Log.d("PinData", "Prepared JSON: $pinDataJson")
        return pinDataJson.toRequestBody("application/json".toMediaTypeOrNull())
    }


    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun getSelectedRange(): Int = when (spinnerAdRange.selectedItemPosition) {
        0 -> 1
        1 -> 2
        2 -> 3
        else -> 0
    }

    private fun getSelectedDuration(): Int = when (spinnerAdDuration.selectedItemPosition) {
        0 -> 2
        1 -> 4
        2 -> 8
        else -> 0
    }
}