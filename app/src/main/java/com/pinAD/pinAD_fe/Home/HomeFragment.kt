package com.pinAD.pinAD_fe.Home

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.pinAD.pinAD_fe.Data.pin.FltPinData
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.PinDetailBottomSheet
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.worker.startLocationService
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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var toggleSwitch: SwitchMaterial
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: LatLng? = null

    companion object {
        private const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1001
        @RequiresApi(Build.VERSION_CODES.Q)
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        toggleSwitch = view.findViewById(R.id.toggle_switch)
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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        setMapStyle(googleMap)
        setupMapCallbacks()
        checkAndRequestPermissions()
    }

    private fun setupMapCallbacks() {
        // 마커 클릭 리스너 설정
        googleMap.setOnMarkerClickListener { marker ->
            val pinId = marker.tag as? Int
            if (pinId != null) {
                lifecycleScope.launch {
                    try {
                        val response = RetrofitInstance.api.getPinData(pinId)
                        if (response.isSuccessful) {
                            val pinWrapper = response.body()
                            if (pinWrapper != null) {  // pinWrapper가 null이 아닌지 체크
                                val pinData = pinWrapper.pin
                                val mediaUrls = pinWrapper.media_urls
                                val coupon = pinWrapper.coupon
                                if (pinData != null) {  // pinData가 null이 아닌지 체크
                                    // pinData와 mediaUrls를 JSON으로 변환하여 전달
                                    val pinJson = Gson().toJson(pinData)
                                    val mediaUrlsJson = Gson().toJson(mediaUrls)
                                    val couponJson = Gson().toJson(coupon)
                                    val bottomSheet = PinDetailBottomSheet.newInstance(pinJson, mediaUrlsJson, couponJson)
                                    bottomSheet.show(parentFragmentManager, bottomSheet.tag)
                                } else {
                                    Toast.makeText(context, "Pin data is missing", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Response body is null", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Failed to fetch pin data", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("PinDataFetch", "Error fetching pin data: ${e.message}")
                        Toast.makeText(context, "Error loading pin data", Toast.LENGTH_SHORT).show()
                    }
                }
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


    private fun createMarkerIcon(borderColor: Int): BitmapDescriptor {
        val size = 100 // Pin size
        val borderWidth = 10 // Border width
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // Draw border
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth.toFloat()
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - borderWidth / 2f, paint)

//        // Draw profile picture
//        val profileBitmap = Bitmap.createBitmap(size - borderWidth * 2, size - borderWidth * 2, Bitmap.Config.ARGB_8888)
//        val profileCanvas = Canvas(profileBitmap)
//
//        val profilePaint = Paint()
//        profilePaint.isAntiAlias = true
//
//        try {
//            // Ensure URL starts with a valid protocol
//            val validProfilePicUrl = if (profilePicUrl.startsWith("http://") || profilePicUrl.startsWith("https://")) {
//                profilePicUrl
//            } else {
//                "https://$profilePicUrl"
//            }
//
//            // Load and draw profile picture
//            val profilePic = BitmapFactory.decodeStream(URL(validProfilePicUrl).openStream())
//            profileCanvas.drawBitmap(profilePic, null, Rect(0, 0, profileBitmap.width, profileBitmap.height), profilePaint)
//
//            // Draw the profile picture inside the border
//            canvas.drawBitmap(profileBitmap, borderWidth.toFloat(), borderWidth.toFloat(), null)
//        } catch (e: Exception) {
//            Log.e("CreateMarkerIcon", "Error loading profile picture: ${e.message}")
//        }
        // Draw default icon (a circle in the center)
        paint.color = Color.WHITE // Icon color
        paint.style = Paint.Style.FILL
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - borderWidth / 2f - 1, paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun loadAndShowPin() {
        val latitude = currentLocation?.latitude?: 37.7749
        val longitude = currentLocation?.longitude?: -122.4194
        val radius = 10000
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
                            Log.d("PinDataFetch", "$title, $description, $isAds")
                            Log.d("PinDataFetch", "Media Files: $mediaFiles")
                            Log.d("PinDebug", "Pin ID: ${pin.id}")

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
                            val markerIcon = createMarkerIcon(borderColor)
                            val marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(pinLocation)
                                    .title(title)
                                    .snippet(description)
                                    .icon(markerIcon)
                            )
//                            val pinJson = Gson().toJson(pin)
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

    private fun loadAndShowOtherPin() {
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
                            Log.d("PinDataFetch", "$title, $description, $isAds")
                            Log.d("PinDataFetch", "Media Files: $mediaFiles")

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
                            val markerIcon = createMarkerIcon(borderColor)
                            val marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(pinLocation)
                                    .title(title)
                                    .snippet(description)
                                    .icon(markerIcon)
                            )
//                            val pinJson = Gson().toJson(pin)
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
                    loadAndShowOtherPin()
                }
            }
        }
    }

    private fun showUserPins() {
        googleMap.clear()
        loadAndShowOtherPin()
    }

    private fun showOtherPins() {
        googleMap.clear()
        loadAndShowPin()
    }
}
