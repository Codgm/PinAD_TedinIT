package com.example.mappin_fe.Home

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException

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

    private val jsonData = """
        [
            {"id":1,"latitude":37.5665,"longitude":126.978,"title":"서울시청","description":"서울특별시의 시청입니다.","category":"광고"},
            {"id":2,"latitude":37.570,"longitude":126.985,"title":"남산타워","description":"서울의 대표적인 관광명소입니다.","category":"광고"},
            {"id":3,"latitude":37.5502,"longitude":126.982,"title":"동대문디자인플라자","description":"서울의 현대적인 건축물입니다.","mainCategory":"문화"},
            {"id":4,"latitude":37.574,"longitude":127.008,"title":"경복궁","description":"조선 왕조의 주요 궁궐입니다.","mainCategory":"문화"},
            {"id":5,"latitude":37.6103,"longitude":126.9811,"title":"홍대","description":"젊음의 거리, 다양한 문화와 예술이 있는 곳입니다.","category":"문화"}
        ]
        """

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
            val pinData = marker.tag as? String
            if (pinData != null) {
                val bottomSheet = PinDetailBottomSheet.newInstance(pinData.toString())
                bottomSheet.show(parentFragmentManager, bottomSheet.tag)
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
                Log.d("PinDataFetch", "Starting pin data fetch")
                val pinDataListString = RetrofitInstance.api.getUserPins()
                Log.d("PinDataFetch", "Fetched Pins: $pinDataListString")

                if (pinDataListString.isEmpty()) {
                    Log.d("PinDataFetch", "No pins received")
                    Toast.makeText(requireContext(), "No pins available", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val jsonArray = JSONArray(pinDataListString)
                val builder = LatLngBounds.Builder()
                var hasPins = false

                for (i in 0 until jsonArray.length()) {
                    val jsonString = jsonArray.getString(i)
                    val jsonObject = JSONObject(jsonString)

                    // location 문자열에서 위도와 경도 추출
                    val locationStr = jsonObject.getString("location")
                    val regex = """POINT \((-?\d+\.?\d*) (-?\d+\.?\d*)\)""".toRegex()
                    val matchResult = regex.find(locationStr)

                    if (matchResult != null) {
                        val (longitude, latitude) = matchResult.destructured
                        val title = jsonObject.optString("title", "제목 없음")
                        val description = jsonObject.optString("description", "설명 없음")
                        val isAds = jsonObject.optBoolean("is_ads", true)
                        val media_files = jsonObject.getString("media")
                        Log.d("PinDataFetch", "Media Files: $media_files")

                        val pinLocation = LatLng(latitude.toDouble(), longitude.toDouble())
                        builder.include(pinLocation)
                        hasPins = true

                        // 광고 여부에 따라 색상 설정
                        val borderColor = if (isAds) {
                            Color.parseColor("#C8E6C9") // 광고용 색상
                        } else {
                            Color.parseColor("#FFAB91") // 일반 핀 색상
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
                        marker?.tag = jsonObject.toString()
                    }
                }

                // 모든 핀을 포함하는 영역으로 카메라 이동
                if (hasPins) {
                    val bounds = builder.build()
                    val padding = 100 // 화면 가장자리와의 여백
                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                    googleMap.animateCamera(cameraUpdate)
                }
            } catch (e: Exception) {
                Log.e("LoadPinData", "Error: ${e.message}", e)
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