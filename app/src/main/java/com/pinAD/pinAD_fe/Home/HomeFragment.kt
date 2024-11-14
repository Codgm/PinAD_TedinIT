package com.pinAD.pinAD_fe.Home

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.pinAD.pinAD_fe.Data.pin.FltPinData
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.PinDetailBottomSheet
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.service.startLocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.pinAD.pinAD_fe.network.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var toggleSwitch: SwitchMaterial
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: LatLng? = null
    private var pinId: String? = null
    private var pinIdToFocus: String? = null
    private var shouldFocusPin: Boolean = false
    private var allMarkers = mutableMapOf<Int, Marker>()
    private var pinDetailJob: Job? = null
    private var isMapReady = false
    private var pendingPinId: Int? = null

    companion object {
        private const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val ARG_PIN_ID = "pin_id"
        @RequiresApi(Build.VERSION_CODES.Q)
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

        fun newInstance(pinId: String?, shouldFocusPin: Boolean = false): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PIN_ID, pinId)
                    putBoolean("should_focus_pin", shouldFocusPin)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        pinId = arguments?.getString(ARG_PIN_ID)
        pinIdToFocus = arguments?.getString(ARG_PIN_ID)
        shouldFocusPin = arguments?.getBoolean("should_focus_pin", false) ?: false

        pendingPinId = pinId?.toIntOrNull()

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        toggleSwitch = view.findViewById(R.id.toggle_switch)

        Log.d("pinID", "$pinId")
        pinId?.toIntOrNull()?.let { id ->
            Log.d("pinID_loading", "Loading pin detail for id: $id")
            loadPinDetail(id)
        }

        toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showUserPins()
            } else {
                showOtherPins()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                setupMap()
                startBackgroundLocationService()
            } else {
                Toast.makeText(
                    requireContext(),
                    "위치 권한이 필요합니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // 사용자가 지도를 직접 조작하지 않는 한, 위치가 자동으로 업데이트되지 않도록 설정
                    if (currentLocation == null) {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        currentLocation?.let {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 14f))
                        }
                    }
                }
            }
        }

        return view
    }



    private fun loadPinDetail(pinId: Int) {
        Log.d("PinDetail", "Starting to load pin detail for id: $pinId")

        // 이전 작업이 있다면 취소
        pinDetailJob?.cancel()

        // 새로운 코루틴 시작
        pinDetailJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                // withContext를 사용하여 네트워크 호출
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getPinData(pinId)
                }

                // Fragment가 유효한지 확인
                if (!isAdded || !isActive) {
                    return@launch
                }

                if (response.isSuccessful) {
                    val pinWrapper = response.body()
                    if (pinWrapper != null) {
                        val pinData = pinWrapper.pin
                        val mediaUrls = pinWrapper.media_urls
                        val coupon = pinWrapper.coupon
                        Log.d("PinDetail", "Received pin data: $pinData")

                        if (pinData != null) {
                            try {
                                val pinJson = Gson().toJson(pinData)
                                val mediaUrlsJson = Gson().toJson(mediaUrls)
                                val couponJson = Gson().toJson(coupon)

                                // Fragment가 여전히 유효한지 다시 확인
                                if (isAdded && parentFragmentManager.isDestroyed.not()) {
                                    val bottomSheet = PinDetailBottomSheet.newInstance(pinJson, mediaUrlsJson, couponJson)

                                    // 이전 BottomSheet가 있다면 제거
                                    val prevBottomSheet = parentFragmentManager.findFragmentByTag("PinDetailBottomSheet")
                                    if (prevBottomSheet != null) {
                                        parentFragmentManager.beginTransaction()
                                            .remove(prevBottomSheet)
                                            .commitAllowingStateLoss()
                                    }

                                    // 새 BottomSheet 표시
                                    Log.d("PinDetail", "Attempting to show bottom sheet")
                                    bottomSheet.show(parentFragmentManager, "PinDetailBottomSheet")
                                }
                            } catch (e: Exception) {
                                Log.e("PinDetail", "Error showing bottom sheet: ${e.message}")
                                context?.let {
                                    Toast.makeText(it, "Error showing pin details", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            context?.let {
                                Toast.makeText(it, "Pin data is missing", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    context?.let {
                        Toast.makeText(it, "Failed to fetch pin data: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    // 코루틴이 취소된 경우는 무시
                    return@launch
                }

                Log.e("PinDataFetch", "Error fetching pin data: ${e.message}")
                if (isAdded) {
                    context?.let {
                        Toast.makeText(it, "Error loading pin data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        setMapStyle(googleMap)
        setupMapCallbacks()
        checkAndRequestPermissions()
        isMapReady = true

        executePostMapReadyTasks()

    }

    private fun executePostMapReadyTasks() {
        if (!isMapReady) return

        setupMap()

        // 지도 초기화가 완료된 후 pendingPinId가 있다면 처리
        pendingPinId?.let { id ->
            // 약간의 지연을 주어 지도 초기화가 완전히 끝나길 보장
            lifecycleScope.launch {
                delay(500) // 0.5초 지연
                if (isAdded && isMapReady) {
                    loadPinDetail(id)
                    if (shouldFocusPin) {
                        focusOnPin(id)
                    }
                }
            }
        }
    }

    private fun setupMapCallbacks() {
        // 마커 클릭 리스너 설정
        googleMap.setOnMarkerClickListener { marker ->
            val markerPinId = marker.tag as? Int
            markerPinId?.let {
                loadPinDetail(it)
            }
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkAndRequestPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) !=
                    PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            setupMap()
            startBackgroundLocationService()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun startBackgroundLocationService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 이상에서는 백그라운드 위치 권한 추가 확인
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                requireActivity().startLocationService()
            } else {
                requestBackgroundLocationPermission()
            }
        } else {
            requireActivity().startLocationService()
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AlertDialog.Builder(requireContext())
                .setTitle("백그라운드 위치 권한 필요")
                .setMessage("앱이 백그라운드에서도 위치 정보를 수집할 수 있도록 '항상 허용'을 선택해주세요.")
                .setPositiveButton("설정") { _, _ ->
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }


    private suspend fun createMarkerIconWithProfile(context: Context, borderColor: Int): BitmapDescriptor {
        return withContext(Dispatchers.IO) {
            val size = 100 // 마커 크기
            val borderWidth = 10 // 테두리 두께
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            try {
                // UserDataManager에서 프로필 URL 가져오기
                val profileUrl = UserDataManager.userData?.profile_picture

                if (!profileUrl.isNullOrEmpty()) {
                    // Glide로 이미지 로드
                    val profileBitmap = try {
                        Glide.with(context)
                            .asBitmap()
                            .load(profileUrl)
                            .apply(RequestOptions.circleCropTransform())
                            .submit(size - borderWidth * 2, size - borderWidth * 2)
                            .get()
                    } catch (e: Exception) {
                        Log.e("CreateMarkerIcon", "Error loading image: ${e.message}")
                        null
                    }

                    if (profileBitmap != null) {
                        // 프로필 이미지 그리기
                        val shader = BitmapShader(profileBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                        val matrix = Matrix()

                        // 이미지 크기 조정
                        val scale = (size - borderWidth * 2f) / profileBitmap.width.coerceAtLeast(profileBitmap.height)
                        matrix.setScale(scale, scale)

                        // 이미지 중앙 정렬
                        matrix.postTranslate(
                            borderWidth + (size - borderWidth * 2 - profileBitmap.width * scale) / 2f,
                            borderWidth + (size - borderWidth * 2 - profileBitmap.height * scale) / 2f
                        )

                        shader.setLocalMatrix(matrix)
                        paint.shader = shader

                        // 프로필 이미지 그리기
                        canvas.drawCircle(
                            size / 2f,
                            size / 2f,
                            size / 2f - borderWidth - 1,
                            paint
                        )
                    } else {
                        drawDefaultBackground(canvas, paint, size, borderWidth)
                    }
                } else {
                    drawDefaultBackground(canvas, paint, size, borderWidth)
                }
            } catch (e: Exception) {
                Log.e("CreateMarkerIcon", "Error creating marker icon: ${e.message}")
                drawDefaultBackground(canvas, paint, size, borderWidth)
            }

            // 테두리 그리기
            paint.apply {
                shader = null
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = borderWidth.toFloat()
            }
            canvas.drawCircle(size / 2f, size / 2f, size / 2f - borderWidth / 2f, paint)

            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    // 기본 배경 그리기 함수
    private fun drawDefaultBackground(canvas: Canvas, paint: Paint, size: Int, borderWidth: Int) {
        paint.apply {
            shader = null
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(
            size / 2f,
            size / 2f,
            size / 2f - borderWidth - 1,
            paint
        )
    }

    private fun createMarkerIcon(borderColor: Int, profilePictureUrl: String): BitmapDescriptor? {
        val size = 100 // 핀 크기
        val borderWidth = 10 // 테두리 두께
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // 테두리 그리기
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth.toFloat()
        paint.isAntiAlias = true
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - borderWidth / 2f, paint)

        // 기본 배경 그리기 (프로필 이미지 로드 전)
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - borderWidth - 1, paint)

        // Glide로 프로필 이미지 로드
        if (profilePictureUrl.isNotEmpty()) {
            try {
                val profileBitmap = context?.let {
                    Glide.with(it)
                        .asBitmap()
                        .load(profilePictureUrl)
                        .submit(size - borderWidth * 2, size - borderWidth * 2) // 이미지 크기 조정
                        .get()
                }

                // 프로필 이미지를 원형으로 그리기
                val shader = profileBitmap?.let { BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }
                paint.shader = shader

                canvas.drawCircle(
                    size / 2f,
                    size / 2f,
                    size / 2f - borderWidth - 1,
                    paint
                )
            } catch (e: Exception) {
                Log.e("CreateMarkerIcon", "Error loading profile picture with Glide", e)
            }
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }




    private fun loadAndShowPin() {
        val latitude = currentLocation?.latitude?: 37.7749
        val longitude = currentLocation?.longitude?: -122.4194
        val radius = 1
        lifecycleScope.launch {
            try {
                // 핀 데이터 가져오기
                val response = RetrofitInstance.api.getPins(latitude, longitude, radius)
                Log.d("PinDataFetch", "$response")

                if (response.isSuccessful) {
                    val pinDataList: List<FltPinData>? = response.body()
                    Log.d("PinData", "$pinDataList")

                    if (pinDataList.isNullOrEmpty()) {
                        Log.d("PinDataFetch", "No pins received")
                        Toast.makeText(requireContext(), "No pins available", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val builder = LatLngBounds.Builder()
                    var hasPins = false

                    val userTagsList = UserDataManager.userData?.tags?.map { it.trim() }?: listOf()

                    // 핀 데이터 처리
                    for (pin in pinDataList) {
                        // 위치 문자열에서 위도와 경도 추출
                        val locationStr = pin.location
                        val regex = """POINT \((-?\d+\.?\d*) (-?\d+\.?\d*)\)""".toRegex()
                        val matchResult = regex.find(locationStr)

                        if (matchResult != null) {
                            val (longitude, latitude) = matchResult.destructured
                            val title = pin.title ?: "제목 없음"
                            val description = pin.description ?: "설명 없음"
                            val isAds = pin.is_ads
                            val mediaFiles = pin.media
                            val profile_picture = pin.profile_picture
                            val pinTagsList = pin.tags?.map { it.name.trim() }?: listOf()
                            Log.d("PinDataFetch", "$title, $description, $isAds")
                            Log.d("PinDataFetch", "Media Files: $mediaFiles")
                            Log.d("PinDataFetch", "User Tags: $userTagsList , Pin Tags: $pinTagsList")

                            val matchingTags = userTagsList.any { userTag ->
                                pinTagsList.any { pinTag ->
                                    userTag.equals(pinTag, ignoreCase = true)
                                }
                            }
                            Log.d("TagMatching", "Tags match: $matchingTags")

                            val pinLocation = LatLng(latitude.toDouble(), longitude.toDouble())
                            builder.include(pinLocation)
                            hasPins = true

                            // 광고 여부에 따른 마커 색상 설정
                            val borderColor = when(pin.pin_type) {
                                1 -> {Color.parseColor("#F44336")}
                                2 -> {Color.parseColor("#9C27B0")}
                                else -> {Color.parseColor("#C8E6C9")}
                            }

                            // 마커 생성
                            val markerIcon = createMarkerIcon(borderColor, profile_picture)
                            val marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(pinLocation)
                                    .title(title)
                                    .snippet(description)
                                    .icon(markerIcon)
                            )

                            if (matchingTags) {
                                marker?.let {
                                    // 색상 애니메이션 시작
                                    allMarkers[pin.id] = it
                                    startBlinkingEffect(it, borderColor)
                                }
                            }
                            Log.d("PinDebug", "Setting marker tag with ID: ${pin.id}")
                            marker?.tag = pin.id // 마커에 핀 데이터를 태그로 추가
                        }
                    }

                    // 모든 핀을 포함하는 영역으로 카메라 이동
                    if (hasPins) {
                        val bounds = builder.build()
                        val padding = 100 // 화면 가장자리와의 여백
                        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                        googleMap.animateCamera(cameraUpdate)
                    }
                    if (shouldFocusPin && pinIdToFocus != null) {
                        focusOnPin(pinIdToFocus?.toIntOrNull())
                    }
                } else {
                    Log.d("PinDataFetch", "No pins received")
                    Toast.makeText(requireContext(), "No pins available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PinDataFetch", "Error: ${e.message}", e)
                Toast.makeText(requireContext(), "Error loading pins: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun focusOnPin(pinId: Int?) {
        pinId?.let { id ->
            allMarkers[id]?.let { marker ->
                // 마커로 카메라 이동
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(marker.position, 17f),
                    1000,
                    null
                )
                // 핀 상세 정보 로드
                loadPinDetail(id)
            }
        }
    }

    private fun loadAndShowUserPin() {
        lifecycleScope.launch {
            try {
                // 핀 데이터 가져오기
                val response = RetrofitInstance.api.getUserPins()
                Log.d("PinDataFetch", "$response")

                if (response.isSuccessful) {
                    val pinDataList: List<FltPinData>? = response.body()
                    Log.d("PinData", "$pinDataList")

                    if (pinDataList.isNullOrEmpty()) {
                        Log.d("PinDataFetch", "No pins received")
                        Toast.makeText(requireContext(), "No pins available", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val builder = LatLngBounds.Builder()
                    var hasPins = false

                    val userTagsList = UserDataManager.userData?.tags?.map { it.trim() }?: listOf()

                    // 핀 데이터 처리
                    for (pin in pinDataList) {
                        // 위치 문자열에서 위도와 경도 추출
                        val locationStr = pin.location
                        val regex = """POINT \((-?\d+\.?\d*) (-?\d+\.?\d*)\)""".toRegex()
                        val matchResult = regex.find(locationStr)

                        if (matchResult != null) {
                            val (longitude, latitude) = matchResult.destructured
                            val title = pin.title ?: "제목 없음"
                            val description = pin.description ?: "설명 없음"
                            val isAds = pin.is_ads
                            val mediaFiles = pin.media
                            val duration = pin.duration
                            val pinType = pin.pin_type
                            val pinTagsList = pin.tags?.map { it.name.trim() }?: listOf()
                            Log.d("PinDataFetch", "$title, $description, $isAds")
                            Log.d("PinDataFetch", "Media Files: $mediaFiles")
                            Log.d("PinDataFetch", "User Tags: $userTagsList , Pin Tags: $pinTagsList")

                            val matchingTags = userTagsList.any { userTag ->
                                pinTagsList.any { pinTag ->
                                    userTag.equals(pinTag, ignoreCase = true)
                                }
                            }
                            Log.d("TagMatching", "Tags match: $matchingTags")

                            val pinLocation = LatLng(latitude.toDouble(), longitude.toDouble())
                            builder.include(pinLocation)
                            hasPins = true

                            // 광고 여부에 따른 마커 색상 설정
                            val borderColor = when(pinType) {
                                1 -> {Color.parseColor("#F44336")}
                                2 -> {Color.parseColor("#9C27B0")}
                                else -> {Color.parseColor("#C8E6C9")}
                            }

                            // 마커 생성
                            val markerIcon = createMarkerIconWithProfile(requireContext(),borderColor)
                            val marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(pinLocation)
                                    .title(title)
                                    .snippet(description)
                                    .icon(markerIcon)
                            )
                            if (matchingTags) {
                                Log.d("TagMatching", "Applying blinking effect to marker")
                                marker?.let {
                                    // 색상 애니메이션 시작
                                    startBlinkingEffect(it, borderColor)
                                }
                            }

                            marker?.tag = pin.id // 마커에 핀 데이터를 태그로 추가
                        }
                    }

                    // 모든 핀을 포함하는 영역으로 카메라 이동
                    if (hasPins) {
                        val bounds = builder.build()
                        val padding = 100 // 화면 가장자리와의 여백
                        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                        googleMap.animateCamera(cameraUpdate)
                    }
                } else {
                    Log.d("PinDataFetch", "No pins received")
                    Toast.makeText(requireContext(), "No pins available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PinDataFetch", "Error: ${e.message}", e)
                Toast.makeText(requireContext(), "Error loading pins: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun startBlinkingEffect(marker: Marker, originalColor: Int) {
        val colorFrom = originalColor
        val colorTo = Color.YELLOW
        val duration = 1000L // 1초

        val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()

            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                val red = (Color.red(colorFrom) + (Color.red(colorTo) - Color.red(colorFrom)) * fraction).toInt()
                val green = (Color.green(colorFrom) + (Color.green(colorTo) - Color.green(colorFrom)) * fraction).toInt()
                val blue = (Color.blue(colorFrom) + (Color.blue(colorTo) - Color.blue(colorFrom)) * fraction).toInt()
                val currentColor = Color.rgb(red, green, blue)

                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        // 현재 마커의 아이콘을 유지하면서 색상만 변경
                        val newIcon = if (marker.tag is Int) {
                            context?.let { createMarkerIconWithProfile(it, currentColor) }
                        } else {
                            val profilePicture = (marker.tag as? String) ?: ""
                            createMarkerIcon(currentColor, profilePicture)
                        }

                        marker.setIcon(newIcon)
                    } catch (e: Exception) {
                        Log.e("BlinkingEffect", "Error updating marker color: ${e.message}")
                    }
                }
            }
        }
        valueAnimator.start()
    }


    private fun setMapStyle(map: GoogleMap) {
        try {
            val mapStyleResId = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                R.raw.map_style_dark // 다크 테마일 때
            } else {
                0  // 라이트 테마일 때
            }
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), mapStyleResId
                )
            )
            if (!success) {
                Log.e("MapStyle", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapStyle", "Can't find style. Error: ", e)
        }
    }

    private fun setupMap() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.apply {
                isZoomControlsEnabled = true
                isScrollGesturesEnabled = true
                isTiltGesturesEnabled = true
                isRotateGesturesEnabled = true
            }

            // 최초 위치 확인
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(currentLocation!!, 14f)
                    )
                    loadAndShowPin()
                }
            }
        }
    }

    private fun showUserPins() {
        googleMap.clear()
        loadAndShowUserPin()
    }

    private fun showOtherPins() {
        googleMap.clear()
        loadAndShowPin()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pinDetailJob?.cancel()
        pinDetailJob = null
    }
}
