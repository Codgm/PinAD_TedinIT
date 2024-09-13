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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mappin_fe.Login_Sign.UserAccount
import com.example.mappin_fe.PinData
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
        setupMap()
        setMapStyle(googleMap)
        loadAndShowPin()
    }


    private fun createMarkerIcon(borderColor: Int, profilePicUrl: String, context: Context): BitmapDescriptor {
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

        // Draw profile picture
        val profileBitmap = Bitmap.createBitmap(size - borderWidth * 2, size - borderWidth * 2, Bitmap.Config.ARGB_8888)
        val profileCanvas = Canvas(profileBitmap)

        val profilePaint = Paint()
        profilePaint.isAntiAlias = true

        try {
            // Ensure URL starts with a valid protocol
            val validProfilePicUrl = if (profilePicUrl.startsWith("http://") || profilePicUrl.startsWith("https://")) {
                profilePicUrl
            } else {
                "https://$profilePicUrl"
            }

            // Load and draw profile picture
            val profilePic = BitmapFactory.decodeStream(URL(validProfilePicUrl).openStream())
            profileCanvas.drawBitmap(profilePic, null, Rect(0, 0, profileBitmap.width, profileBitmap.height), profilePaint)

            // Draw the profile picture inside the border
            canvas.drawBitmap(profileBitmap, borderWidth.toFloat(), borderWidth.toFloat(), null)
        } catch (e: Exception) {
            Log.e("CreateMarkerIcon", "Error loading profile picture: ${e.message}")
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }



    private fun loadAndShowPin() {
        val sharedPreferences = requireActivity().getSharedPreferences("PinData", Context.MODE_PRIVATE)
        val pinData = sharedPreferences.getString("last_pin", null)

        pinData?.let {
            val data = it.split(",")
            if (data.size >= 10) {
                val latitude = data[0].toDouble()
                val longitude = data[1].toDouble()
                val title = data[2]
                val range = data[3].toInt()
                val duration = data[4].toInt()
                val mainCategory = data[5]
                val subCategory = data[6]
                val mediaUri = data[7]
                val contentData = data[8]
                val tags = data[9].split("|")

                val pinLocation = LatLng(latitude, longitude)

                val borderColor = when (subCategory) {
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

                UserUtils.fetchUserDetails { nickname, profilePicUrl ->
                    val markerIcon = createMarkerIcon(borderColor, profilePicUrl, requireContext())

                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(pinLocation)
                            .title(title)
                            .icon(markerIcon)
                    )

                    marker?.tag = PinData(title, range, duration, mainCategory, subCategory, mediaUri, contentData, tags)

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pinLocation, 15f))
                }
            }
        }

        googleMap.setOnMarkerClickListener { marker ->
            val pinData = marker.tag as? PinData
            if (pinData != null) {
                val bottomSheet = PinDetailBottomSheet.newInstance(pinData)
                bottomSheet.show(childFragmentManager, bottomSheet.tag)
            }
            true
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
