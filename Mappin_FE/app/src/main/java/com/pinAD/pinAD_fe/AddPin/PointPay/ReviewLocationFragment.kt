package com.pinAD.pinAD_fe.AddPin.Review

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.pinAD.pinAD_fe.AddPin.Camera.MediaFile
import com.pinAD.pinAD_fe.Data.pin.FTag
import com.pinAD.pinAD_fe.Data.user_data.ProfileData
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.Data.pin_review.ReviewRequest
import com.pinAD.pinAD_fe.MainActivity
import com.pinAD.pinAD_fe.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pinAD.pinAD_fe.network.UserDataManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.Date
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.util.UUID

class ReviewLocationFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var pointsTextView: TextView
    private lateinit var nextButton: Button
    private var mediaFiles = mutableListOf<MediaFile>()
    private var selectedLocation: LatLng? = null
    private lateinit var locationSearchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var mediaFilesRecyclerView: RecyclerView
    private var title: String = ""
    private var description: String = ""
    private var category: String = ""
    private var info: String = ""
    private var isAds: Boolean = false
    private var selectedTags: Array<String> = arrayOf()
    private var initialPoints: Int = 500 // 기본 포인트
    private var totalPoints: Int = initialPoints
    private var isSubmitting: Boolean = false // isSubmitting 변수 추가
    private lateinit var attachShortFormVideoButton: Button
    private lateinit var attachReceiptButton: Button
    private var isShortFormVideoUploaded = false
    private var isReceiptUploaded = false
    private val visibility = "public"
    private var pin_type: Int = 0
    private var currentLocation: LatLng? = null

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 100
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_review_location, container, false)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        mediaFilesRecyclerView = view.findViewById(R.id.mediaFilesRecyclerView)
        mediaFilesRecyclerView.layoutManager = LinearLayoutManager(context)
        attachShortFormVideoButton = view.findViewById(R.id.attachShortFormVideoButton)
        attachReceiptButton = view.findViewById(R.id.attachReceiptButton)


        // Bundle에서 이전 Fragment의 데이터 받기
        arguments?.let { bundle ->
            title = bundle.getString("TITLE", "")
            Log.d("Reviewfragment", "타이틀: $title")
            description = bundle.getString("DESCRIPTION", "")
            Log.d("Reviewfragment", "설명: $description")
            category = bundle.getString("CATEGORY", "")
            Log.d("Reviewfragment", "카테고리: $category")
            info = bundle.getString("INFO", "")
            Log.d("Reviewfragment", "정보: $info")
            isAds = bundle.getBoolean("is_Ads", false)
            Log.d("Reviewfragment", "광고 여부: $isAds")
            selectedTags = bundle.getStringArray("SELECTED_TAGS") ?: arrayOf()
            Log.d("Reviewfragment", "선택된 태그: ${selectedTags.joinToString(", ")}")
            pin_type = bundle.getInt("PIN_TYPE")
            Log.d("Reviewfragment", "pin_type: $pin_type")
            val mediaFilesJson = bundle.getString("MEDIA_FILES")
            Log.d("mediafile", "미디어 파일 JSON: $mediaFilesJson")
            if (mediaFilesJson != null) {
                val tempMediaFiles: List<MediaFile> = Gson().fromJson(mediaFilesJson, object : TypeToken<List<MediaFile>>() {}.type)
                mediaFiles.addAll(tempMediaFiles)
                Log.d("Umediafile", "미디어 파일 추가 완료: ${mediaFiles.size}개의 파일이 추가되었습니다.") // 미디어 파일 추가 로그
            } else {
                Log.d("Umediafile", "미디어 파일 JSON이 null입니다.") // 미디어 파일 JSON이 null일 때 로그
            }
        }

        initializeViews(view)
        calculateInitialPoints()
        displayPoints()
        return view
    }

    private fun getUserLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        try {
            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                val location = task.result
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
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


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun initializeViews(view: View) {
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(null)
        mapView.getMapAsync(this)

        pointsTextView = view.findViewById(R.id.pointsTextView)
        nextButton = view.findViewById(R.id.nextButton)
        val attachShortFormVideoButton: Button = view.findViewById(R.id.attachShortFormVideoButton)
        val attachReceiptButton: Button = view.findViewById(R.id.attachReceiptButton)
        locationSearchEditText = view.findViewById(R.id.locationSearchEditText)
        searchButton = view.findViewById(R.id.searchButton)


        searchButton.setOnClickListener {
            val searchQuery = locationSearchEditText.text.toString()
            if (searchQuery.isNotEmpty()) {
                searchLocation(searchQuery)
            }
        }

        attachShortFormVideoButton.setOnClickListener {
            // 파일 선택기 열기
            openFilePicker("video/*") // 비디오 파일 선택기 호출
        }

        attachReceiptButton.setOnClickListener {
            // 파일 선택기 열기
            openFilePicker("image/*") // 이미지 파일 선택기 호출
        }

        nextButton.setOnClickListener {
            if (selectedLocation != null) {
                submitReview()
            } else {
                Toast.makeText(context, "위치를 선택해주세요", Toast.LENGTH_SHORT).show()
            }
        }
        displayMediaFiles()
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

    private fun displayMediaFiles() {
        val adapter = object : RecyclerView.Adapter<MediaViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.media_item_layout, parent, false)
                return MediaViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
                val mediaFile = mediaFiles[position]
                holder.bind(mediaFile)
            }

            override fun getItemCount() = mediaFiles.size
        }

        mediaFilesRecyclerView.adapter = adapter
    }

    private var currentReplaceIndex = -1

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mediaPreview: ImageView = itemView.findViewById(R.id.mediaPreview)
        private val fileTypeText: TextView = itemView.findViewById(R.id.fileTypeText)
        private val fileNameText: TextView = itemView.findViewById(R.id.fileNameText)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        private val changeButton: Button = itemView.findViewById(R.id.changeButton)

        fun bind(mediaFile: MediaFile) {
            // 파일 타입에 따른 텍스트 설정
            when (mediaFile.type) {
                "short_form_video" -> {
                    fileTypeText.text = "숏폼 영상"
                    // 비디오 썸네일 설정
                    setVideoThumbnail(mediaFile.uri)
                }
                "receipt" -> {
                    fileTypeText.text = "영수증"
                    // 이미지 미리보기 설정
                    setImagePreview(mediaFile.uri)
                }
                else -> {
                    fileTypeText.text = "이미지"
                    setImagePreview(mediaFile.uri)
                }
            }

            // 파일 이름 표시
            val fileName = File(mediaFile.uri).name
            fileNameText.text = fileName

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // 포인트 차감
                    when (mediaFile.type) {
                        "short_form_video" -> {
                            if (isShortFormVideoUploaded == true) {
                                totalPoints -= 200
                                isShortFormVideoUploaded = false // 포인트 차감 플래그 설정
                            }
                        }
                        "receipt" -> {
                            if (isReceiptUploaded == true) {
                                totalPoints -= 300
                                isReceiptUploaded = false // 포인트 차감 플래그 설정
                            }
                        }
                    }
                    mediaFiles.removeAt(position)
                    mediaFilesRecyclerView.adapter?.notifyItemRemoved(position)
                    displayPoints()
                }
            }

            changeButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openFilePicker(
                        if (mediaFile.type == "short_form_video") "video/*" else "image/*",
                        position
                    )
                }
            }
        }

        private fun setVideoThumbnail(path: String) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(path)

                // 영상 길이 확인 (밀리초 단위)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMs = durationStr?.toLongOrNull() ?: 0L

                if (durationMs < 60 * 1000) {  // 1분(60초) 미만인 경우에만 썸네일 생성
                    val bitmap = retriever.getFrameAtTime(0)
                    mediaPreview.setImageBitmap(bitmap)
                } else {
                    // 영상 길이가 1분 이상일 경우 기본 비디오 아이콘 표시
                    mediaPreview.setImageResource(android.R.drawable.ic_media_play)
                    Toast.makeText(context, "1분 미만의 영상만 업로드 가능합니다.", Toast.LENGTH_SHORT).show()
                }

                retriever.release()
            } catch (e: Exception) {
                // 예외 발생 시 기본 비디오 아이콘 표시
                mediaPreview.setImageResource(android.R.drawable.ic_media_play)
                Toast.makeText(context, "영상 썸네일 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }


        private fun setImagePreview(path: String) {
            try {
                // 이미지 크기 조절을 위한 옵션 설정
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(path, options)

                // 적절한 크기로 스케일링
                options.inJustDecodeBounds = false
                options.inSampleSize = calculateInSampleSize(options, 80, 80)

                val bitmap = BitmapFactory.decodeFile(path, options)
                mediaPreview.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // 이미지 로드 실패시 기본 이미지 아이콘 표시
                mediaPreview.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }

        private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2

                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            return inSampleSize
        }
    }

    private fun openFilePicker(mimeType: String, replaceIndex: Int = -1) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = mimeType
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        currentReplaceIndex = replaceIndex
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }


    // onActivityResult 메서드에서 선택된 파일 처리
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val mediaFilePath = getRealPathFromUri(uri)
                if (mediaFilePath != null) {
                    val type = if (isVideoFile(uri)) "short_form_video" else "receipt"
                    val newMediaFile = MediaFile(mediaFilePath, type)

                    if (currentReplaceIndex >= 0) {
                        // 파일 변경시 포인트 변동 없음
                        mediaFiles[currentReplaceIndex] = newMediaFile
                        Log.d("mediapath", "미디어 파일 변경: ${mediaFilePath}, 인덱스: $currentReplaceIndex") // 파일 변경 로그
                    } else {
                        // 새 파일 추가시에만 포인트 증가
                        mediaFiles.add(newMediaFile)
                        when (type) {
                            "short_form_video" -> {
                                if (!isShortFormVideoUploaded) {
                                    totalPoints += 200
                                    isShortFormVideoUploaded = true // 포인트 변동을 방지하기 위해 플래그 설정
                                }
                            }
                            "receipt" -> {
                                if (!isReceiptUploaded) {
                                    totalPoints += 300
                                    isReceiptUploaded = true // 포인트 변동을 방지하기 위해 플래그 설정
                                }
                            }
                        }
                    }

                    Log.d("mediaFiles", "$mediaFiles")
                    displayMediaFiles()
                    displayPoints()
                }
            }
        }
    }

    private fun calculateInitialPoints() {
        totalPoints = initialPoints // 기본 포인트로 초기화

        // 최초 업로드된 파일들에 대해서만 포인트 계산
        for (file in mediaFiles) {
            when (file.type) {
                "short_form_video" -> totalPoints += 200
                "receipt" -> totalPoints += 300
            }
        }
    }

    private fun isVideoFile(uri: Uri): Boolean {
        val mimeType = requireContext().contentResolver.getType(uri)
        return mimeType != null && mimeType.startsWith("video/")
    }

    private fun displayPoints() {
        // 포인트를 텍스트뷰 등 UI에 표시하는 로직
        pointsTextView.text = "Total Points: $totalPoints"
    }

    private fun attachMediaFile(uri: Uri, type: String) {
        val mediaFilePath = getRealPathFromUri(uri)
        if (mediaFilePath != null) {
            // 미디어 파일을 mediaFiles 리스트에 추가
            mediaFiles = (mediaFiles + MediaFile(mediaFilePath, type)).toMutableList()
            Log.d(TAG, "Attached media file: $mediaFilePath, Type: $type")
            // 포인트 계산
            calculateInitialPoints() // 포인트를 재계산
            displayPoints() // 포인트 표시 업데이트
        } else {
            Log.e(TAG, "Failed to get file path from URI: $uri")
        }
    }

    private fun getRealPathFromUri(contentUri: Uri): String? {
        val cursor = requireContext().contentResolver.query(contentUri, null, null, null, null)
        return cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            it.moveToFirst()
            it.getString(columnIndex)
        }
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 지도 클릭 리스너 설정
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            selectedLocation = latLng
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
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun createPinDataResponse(nickname: Int): ReviewRequest? {
        val now = Date()
        val location = selectedLocation?.latitude?.let { selectedLocation?.longitude?.let { it1 ->
            LatLng(it,
                it1
            )
        } }
        if (selectedLocation == null && currentLocation != null) {
            selectedLocation = currentLocation
        }
        if (selectedLocation == null) {
            Toast.makeText(context, "위치 정보를 가져올 수 없습니다. 위치를 선택해주세요.", Toast.LENGTH_SHORT).show()
        }
        val tags = selectedTags.map { FTag(it) }
        val infoJsonStr = info
        val processedInfo = try {
            val originalInfoJson = JSONObject(infoJsonStr)

            // 새로운 JSONObject를 생성하여 원래 타입 유지하면서 데이터 복사
            val processedInfoJson = JSONObject()

            // 각 필드에 대해 적절한 타입으로 처리
            originalInfoJson.keys().forEach { key ->
                when (key) {
                    "advantages" -> processedInfoJson.put(key, originalInfoJson.getString(key))
                    "disadvantages" -> processedInfoJson.put(key, originalInfoJson.getString(key))
                    // 필요한 경우 다른 필드들에 대한 처리 추가
                    else -> {
                        // 타입을 자동으로 감지하여 처리
                        val value = originalInfoJson.get(key)
                        when (value) {
                            is Int -> processedInfoJson.put(key, originalInfoJson.getInt(key))
                            is Double -> processedInfoJson.put(key, originalInfoJson.getDouble(key))
                            is Boolean -> processedInfoJson.put(key, originalInfoJson.getBoolean(key))
                            else -> processedInfoJson.put(key, originalInfoJson.getString(key))
                        }
                    }
                }
            }
            processedInfoJson.toString()
        } catch (e: Exception) {
            Log.e("PinData", "Error processing info JSON", e)
            infoJsonStr // 에러 발생시 원본 문자열 반환
        }

        return ReviewRequest(
            id = UUID.randomUUID().toString(),
            latitude = selectedLocation?.latitude ?: 0.0,
            longitude = selectedLocation?.longitude ?: 0.0,
            location = location.toString(),
            pin_type = pin_type,
            user = nickname.toString().toIntOrNull() ?: 0,
            title = title,
            description = description,
            media_files = mediaFiles.map { it.uri },  // MediaFile의 uri를 String으로 변환
            info = processedInfo,
            tags = tags,
            visibility = visibility,
            is_ads = isAds,
            created_at = now,
            updated_at = Date(now.time)
        )
    }

    private fun submitReview() {
        if (isSubmitting) return
        isSubmitting = true

        lifecycleScope.launch {
            // 사용자 프로필 가져오기
            val profileData = UserDataManager.getUserData()
            val nickname = profileData?.nickname?.toIntOrNull() ?: 0 // 기본값 설정

            val pinData = createPinDataResponse(nickname) ?: run {
                isSubmitting = false
                return@launch
            }

            // 데이터 유효성 검사
            if (pinData.title.isBlank() || pinData.description.isBlank()) {
                Toast.makeText(context, "제목과 설명을 입력해주세요", Toast.LENGTH_SHORT).show()
                isSubmitting = false
                return@launch
            }

            Log.d("pindata", "Submitting Review: Title: ${pinData.title}, Description: ${pinData.description}, " +
                    "Location: ${pinData.latitude}, ${pinData.longitude}, Tags: ${pinData.tags.joinToString(", ")}")

            // MultipartBody.Part 리스트 생성
            val mediaParts = mutableListOf<MultipartBody.Part>()

            // 미디어 파일 처리
            mediaFiles.forEach { mediaFile ->
                try {
                    val file = File(mediaFile.uri)
                    if (!file.exists()) {
                        Log.e(TAG, "File does not exist: ${file.absolutePath}")
                        return@forEach
                    }

                    // 파일 크기 체크
                    if (file.length() > 100 * 1024 * 1024) { // 100MB 제한
                        Toast.makeText(context, "파일 크기는 100MB를 초과할 수 없습니다", Toast.LENGTH_SHORT).show()
                        isSubmitting = false
                        return@forEach
                    }

                    // 비디오 파일인 경우 길이 체크
                    if (mediaFile.type == "short_form_video") {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(file.absolutePath)
                        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
                        if (duration > 60 * 1000) { // 1분 초과
                            Toast.makeText(context, "비디오는 1분을 초과할 수 없습니다", Toast.LENGTH_SHORT).show()
                            isSubmitting = false
                            return@forEach
                        }
                        retriever.release()
                    }

                    // 파일 타입에 따른 적절한 MimeType 설정
                    val mimeType = when (mediaFile.type) {
                        "short_form_video" -> "video/*"
                        "receipt" -> "image/*"
                        else -> "image/*"
                    }

                    val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData(
                        "media_files",
                        file.name,
                        requestFile
                    )
                    mediaParts.add(part)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing media file: ${e.message}")
                }
            }

            // RequestBody 객체들 생성
            val titleBody = pinData.title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = pinData.description.toRequestBody("text/plain".toMediaTypeOrNull())
            val latitudeBody = pinData.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val longitudeBody = pinData.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val infoPart = pinData.info.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val isAdsBody = pinData.is_ads.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val visibilityBody = pinData.visibility.toRequestBody("text/plain".toMediaTypeOrNull())
            val pintypePart = pinData.pin_type.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val tagsParts = java.util.ArrayList<RequestBody>()
            // Tag 객체에서 name 필드를 사용하여 RequestBody로 변환
            pinData.tags.forEach { tag ->
                tagsParts.add(tag.name.toRequestBody("text/plain".toMediaTypeOrNull())) // 수정된 부분
            }

            // API 호출
            try {
                val response = RetrofitInstance.api.saveReviewDataWithMedia(
                    title = titleBody,
                    description = descriptionBody,
                    latitude = latitudeBody,
                    longitude = longitudeBody,
                    media_files = mediaParts,
                    info = infoPart,
                    pin_type = pintypePart,
                    tag_ids = tagsParts,
                    visibility = visibilityBody,
                    is_ads = isAdsBody
                )

                if (response.isSuccessful) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "리뷰가 성공적으로 등록되었습니다!", Toast.LENGTH_SHORT).show()
                        showPointsEarnedDialog(totalPoints)
                    }
                    navigateToMain()
                } else {
                    val errorBody = response.errorBody()?.string()
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "등록 실패: ${errorBody ?: "알 수 없는 오류가 발생했습니다"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting review: ${e.message}", e)
                activity?.runOnUiThread {
                    Toast.makeText(
                        context,
                        "네트워크 오류가 발생했습니다: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                isSubmitting = false
            }
        }
    }


    private fun showPointsEarnedDialog(points: Int) {
        // 포인트 획득 다이얼로그 구현
        // 예: MaterialAlertDialogBuilder 사용
    }

    private fun navigateToMain() {
        activity?.let {
            val intent = Intent(it, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            it.finish() // 현재 Fragment를 닫습니다.
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
}