package com.example.mappin_fe.Home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
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
import com.example.mappin_fe.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.switchmaterial.SwitchMaterial

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

    private fun loadAndShowPin() {
        val sharedPreferences = requireActivity().getSharedPreferences("PinData", Context.MODE_PRIVATE)
        val pinData = sharedPreferences.getString("last_pin", null)

        pinData?.let {
            val data = it.split(",")
            if (data.size == 5) {
                val latitude = data[0].toDouble()
                val longitude = data[1].toDouble()
                val title = data[2]
                val range = data[3].toInt()
                val duration = data[4].toInt()

                val pinLocation = LatLng(latitude, longitude)
                googleMap.addMarker(MarkerOptions().position(pinLocation).title("$title ($range km, $duration hours)"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pinLocation, 15f))
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
        val userLocation = LatLng(37.7749, -122.4194) // 예시: 사용자 위치
        googleMap.addMarker(MarkerOptions().position(userLocation).title("내 핀"))
    }

    private fun showOtherPins() {
        googleMap.clear()
        val otherLocation = LatLng(34.0522, -118.2437) // 예시: 다른 사용자의 핀 위치
        googleMap.addMarker(MarkerOptions().position(otherLocation).title("타인의 핀"))
    }
}
