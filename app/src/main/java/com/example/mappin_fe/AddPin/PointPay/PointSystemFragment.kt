package com.example.mappin_fe.AddPin.PointPay

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
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
    private lateinit var receivedSubCategory: String
    private lateinit var receivedMainCategory: String
    private lateinit var info: String // contentData 대신 info 사용
    private lateinit var title: String // title 추가
    private lateinit var description: String // description 추가
    private var is_ads: Boolean = false // is_ads 추가
    private var selectedTags: List<String> = listOf()
    private lateinit var switchVisibility: Switch
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var mediafiles: List<MediaFile>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_point_system, container, false)
        initializeViews(view)
        setupSpinners()
        updateCurrentPointsText()
        setupListeners()
        return view
    }

    private fun initializeViews(view: View) {
        receivedSubCategory = arguments?.getString("SELECTED_SUBCATEGORY") ?: ""
        receivedMainCategory = arguments?.getString("SELECTED_MAIN_CATEGORY") ?: ""
        info = arguments?.getString("INFO") ?: ""
        title = arguments?.getString("TITLE") ?: "" // title 수신
        description = arguments?.getString("DESCRIPTION") ?: "" // description 수신
        is_ads = arguments?.getBoolean("is_Ads", false) ?: false // is_ads 수신
        selectedTags = arguments?.getStringArray("SELECTED_TAGS")?.toList() ?: emptyList()
        // 미디어 파일 정보 받기
        arguments?.let {
            val mediaFilesJson = it.getString("MEDIA_FILES")
            mediafiles = Gson().fromJson(mediaFilesJson, object : TypeToken<List<MediaFile>>() {}.type)
        }
        Log.d("PointSystemFragment", "Number of media files: ${mediafiles.size}")
        Log.d("PointSystemFragment", "Received Main Category: $receivedMainCategory")
        Log.d("PointSystemFragment", "Received Sub Category: $receivedSubCategory")
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
            UserUtils.fetchUserDetails { nickname, _ ->
                if (calculateAndUpdatePoints()) {
                    val pinData = createPinData(nickname)
                    sendPinDataToServer(pinData)
                    navigateToMainActivity()
                } else {
                    Toast.makeText(context, "Insufficient points!", Toast.LENGTH_SHORT).show()
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
        return PinDataResponse(
            id = UUID.randomUUID().toString(),
            latitude = 37.7749,
            longitude = -122.4194,
            location = location.toString(),
            user = nickname.toIntOrNull() ?: 0,
            title = title, // title 추가
            description = description, // description 추가
            range = getSelectedRange(),
            duration = getSelectedDuration(),
            mainCategory = receivedMainCategory,
            subCategory = receivedSubCategory,
            mediafiles = mediafiles.map { it.uri },
            info = info,
            tags = selectedTags,
            visibility = if (switchVisibility.isChecked) "public" else "private",
            is_ads = is_ads, // is_ads 추가
            created_at = now,
            updated_at = Date(now.time + (getSelectedDuration() * 60 * 60 * 1000))
        )
    }

    private fun sendPinDataToServer(pinData: PinDataResponse) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.savePinData(pinData)
                if (response.isSuccessful) {
                    Log.d("PinData", "Pin data saved successfully: ${response.body()}")
                    response.body()?.let { savedPinData ->
                        uploadMediaFiles(savedPinData.id)
                    } ?: run {
                        Log.e("PinData", "Saved pin data is null")
                        Toast.makeText(context, "Error: Saved pin data is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("PinData", "Error saving pin data: HTTP ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Error saving pin data: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PinData", "Exception saving pin data", e)
                Toast.makeText(context, "Exception saving pin data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private suspend fun uploadMediaFiles(pinId: String) {
        withContext(Dispatchers.IO) {
            for (media in mediafiles) {
                try {
                    val file = getFileFromUri(media.uri)
                    file?.let {
                        val requestFile = it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                        val part = MultipartBody.Part.createFormData("file", it.name, requestFile)
                        val response = RetrofitInstance.api.uploadMedia(pinId, part)
                        if (response.isSuccessful) {
                            Log.d("MediaUpload", "Media uploaded successfully: ${response.body()}")
                        } else {
                            Log.e("MediaUpload", "Error uploading media: HTTP ${response.code()} - ${response.errorBody()?.string()}")
                        }
                    } ?: Log.e("MediaUpload", "File is null for URI: ${media.uri}")
                } catch (e: Exception) {
                    Log.e("MediaUpload", "Exception uploading media", e)
                }
            }
        }
        withContext(Dispatchers.Main) {
            navigateToMainActivity()
        }
    }

    private fun getFileFromUri(uri: String): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(Uri.parse(uri))
            val tempFile = File.createTempFile("media", ".jpg", requireContext().cacheDir)
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("FileError", "Error getting file from URI: $uri", e)
            null
        }
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