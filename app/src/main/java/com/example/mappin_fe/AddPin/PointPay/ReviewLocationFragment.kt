package com.example.mappin_fe.AddPin.Review

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mappin_fe.AddPin.Camera.MediaFile
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.Data.ReviewRequest
import com.example.mappin_fe.MainActivity
import com.example.mappin_fe.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.Date
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

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 100
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
            Log.d("title", "타이틀: $title")
            description = bundle.getString("DESCRIPTION", "")
            Log.d("description", "설명: $description")
            category = bundle.getString("CATEGORY", "")
            Log.d("category", "카테고리: $category")
            info = bundle.getString("INFO", "")
            Log.d("info", "정보: $info")
            isAds = bundle.getBoolean("is_Ads", false)
            Log.d("isAds", "광고 여부: $isAds")
            selectedTags = bundle.getStringArray("SELECTED_TAGS") ?: arrayOf()
            Log.d("tags", "선택된 태그: ${selectedTags.joinToString(", ")}")
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

    private fun showUploadMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
                val bitmap = retriever.getFrameAtTime(0)
                mediaPreview.setImageBitmap(bitmap)
                retriever.release()
            } catch (e: Exception) {
                // 썸네일 생성 실패시 기본 비디오 아이콘 표시
                mediaPreview.setImageResource(android.R.drawable.ic_media_play)
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

        // 서울 중심으로 초기 위치 설정
        val seoul = LatLng(37.5665, 126.9780)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15f))

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
    }

    private fun submitReview() {
        if (isSubmitting) return
        isSubmitting = true

        val now = Date()

        // ReviewRequest 객체 생성
        val reviewRequest = ReviewRequest(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            category = category,
            Info = info,
            tags = selectedTags.toList(),
            latitude = selectedLocation?.latitude ?: 0.0,
            longitude = selectedLocation?.longitude ?: 0.0,
            points = totalPoints,
            isAds = isAds,
            mediaFiles = mediaFiles.map { it.uri },
            created_at = now,
        )

        // 코루틴을 사용하여 API 호출
        lifecycleScope.launch {
            isSubmitting = true // 제출 중 상태로 변경

            try {
                val response = RetrofitInstance.api.submitReview(reviewRequest) // API 호출

                if (response.isSuccessful) {
                    handleSuccessfulSubmission(response.toString()) // 성공적인 제출 처리
                } else {
                    showError(response.message()) // 에러 메시지 표시
                }
            } catch (e: Exception) {
                showError(e.message ?: "Unknown error occurred") // 예외 처리
            } finally {
                isSubmitting = false // 제출 중 상태 해제
            }
        }
    }

    private fun handleSuccessfulSubmission(reviewId: String?) {
        // 성공 토스트 메시지 표시
        Toast.makeText(context, "리뷰가 성공적으로 등록되었습니다!", Toast.LENGTH_SHORT).show()

        // 포인트 획득 다이얼로그 표시
        showPointsEarnedDialog(totalPoints)

        // 메인 화면으로 이동
        navigateToMain()
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

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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