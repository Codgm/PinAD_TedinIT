package com.example.mappin_fe.Home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mappin_fe.Data.PinDataResponse
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.PinDetailBottomSheet
import com.example.mappin_fe.R
import com.example.mappin_fe.UserUtils
import com.google.android.gms.location.*
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
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.URL

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var toggleSwitch: SwitchMaterial
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: LatLng? = null

    private val defaultLocation = LatLng(37.5042, 126.9537) // 서울 상도동

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

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setupMap()
            } else {
                // 권한이 거부된 경우 처리할 로직을 추가할 수 있습니다.
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
                            addSampleMarkers(it)
                        }
                    }
                }
            }
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        setMapStyle(googleMap)
        // 마커 클릭 리스너 설정
        googleMap.setOnMarkerClickListener { marker ->
            val pinData = marker.tag as? PinDataResponse
            if (pinData != null) {
                val bottomSheet = PinDetailBottomSheet.newInstance(pinData.toString())
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
            true
        }
        setupMap()
        loadAndShowPin()
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
        lifecycleScope.launch {
            try {
                // 서버에서 핀 데이터 목록을 가져오기
                val pinDataList = RetrofitInstance.api.getUserPins() // API 호출

                // 핀 데이터를 지도에 표시
                pinDataList.forEach { pinDataResponse ->
                    // 위치 데이터 추출
                    val latitude = pinDataResponse.latitude
                    val longitude = pinDataResponse.longitude
                    if (latitude != null && longitude != null) {
                        val pinLocation = LatLng(latitude, longitude)

                    // 서브카테고리에 따라 색상 설정
                    val borderColor = when (pinDataResponse.subCategory?: "") {
                        "유통" -> Color.parseColor("#C8E6C9")
                        "F&B" -> Color.parseColor("#FFAB91")
                        "행사 알림" -> Color.parseColor("#64B5F6")
                        "리뷰" -> Color.parseColor("#D1C4E9")
                        "명소 추천" -> Color.parseColor("#FFD54F")
                        "약속 장소" -> Color.parseColor("#4CAF50")
                        "여행 메모" -> Color.parseColor("#303F9F")
                        "할인 요청" -> Color.parseColor("#EF5350")
                        else -> Color.parseColor("#FFAB91")
                    }

                    // 기본 아이콘으로 마커 생성
                    val markerIcon = createMarkerIcon(borderColor)

                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(pinLocation)
                            .title(pinDataResponse.title ?: "제목 없음")
                            .snippet(pinDataResponse.description ?: "설명 없음")
                            .icon(markerIcon)
                    )

                    marker?.tag = pinDataResponse
                    }
                }
                // 모든 핀을 포함하는 영역으로 카메라 이동
                if (pinDataList.isNotEmpty()) {
                    val builder = LatLngBounds.Builder()
                    pinDataList.forEach { pin ->
                        if (pin.latitude != null && pin.longitude != null) {
                            builder.include(LatLng(pin.latitude, pin.longitude))
                        }
                    }
                    val bounds = builder.build()
                    val padding = 100 // 화면 가장자리와의 여백
                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                    googleMap.animateCamera(cameraUpdate)
                }
            } catch (e: HttpException) {
                Log.e("LoadPinData", "Error fetching pin data: ${e.message()}")
                Toast.makeText(requireContext(), "Error fetching pin data", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LoadPinData", "Error: ${e.message}")
                Toast.makeText(requireContext(), "Unexpected error", Toast.LENGTH_SHORT).show()
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        // 지도 기본 설정
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true
        googleMap.uiSettings.isTiltGesturesEnabled = true
        googleMap.uiSettings.isRotateGesturesEnabled = true

        // 서울 상도동으로 초기 위치 설정
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f))
        addSampleMarkers(defaultLocation)

        // 요청한 위치 업데이트
        requestLocationUpdate()
    }

    private fun requestLocationUpdate() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun addSampleMarkers(center: LatLng) {
        googleMap.addMarker(MarkerOptions().position(center).title("현재 위치"))

        val nearbyLocations = listOf(
            LatLng(center.latitude + 0.01, center.longitude),
            LatLng(center.latitude - 0.01, center.longitude),
            LatLng(center.latitude, center.longitude + 0.01),
            LatLng(center.latitude, center.longitude - 0.01)
        )

        nearbyLocations.forEachIndexed { index, latLng ->
            googleMap.addMarker(MarkerOptions().position(latLng).title("샘플 핀 $index"))
        }
    }

    private fun showUserPins() {
        googleMap.clear()
        loadAndShowPin()
    }

    private fun showOtherPins() {
        googleMap.clear()
        val otherLocation = LatLng(34.0522, -118.2437) // 예시: 다른 사용자의 핀 위치
        googleMap.addMarker(MarkerOptions().position(otherLocation).title("타인의 핀"))
    }
}
