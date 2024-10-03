package com.example.mappin_fe.Search

import android.annotation.SuppressLint
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
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mappin_fe.Data.PinDataResponse
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.PinDetailBottomSheet
import com.example.mappin_fe.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONArray

class SearchFragment : Fragment(), OnMapReadyCallback {

    private lateinit var searchBar: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var pinAdapter: PinAdapter
    private val searchHistory = mutableListOf<String>()
    private val searchResults = mutableListOf<PinDataResponse>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: LatLng? = null

    private var map: GoogleMap? = null
    private var isMapReady = false

    val jsonString = """ [{"id":34,"user":7,"location":"SRID=4326;POINT (-122.084 37.4219983)","title":"3% Discount","description":"Smart Phone","media":"http://7636-175-198-127-14.ngrok-free.app/media/pins_images/2024-10-02-06-31-46-902.jpg","is_ads":true,"info":"{\"range\":3,\"duration\":8,\"additionalInfo\":\"{\\\"field1\\\":\\\"Samsung\\\",\\\"field2\\\":\\\"240\\\",\\\"field3\\\":\\\"3%\\\"}\"}","tags":[{"id":2,"name":"전자기기"},{"id":6,"name":"베스트셀러"}],"created_at":"2024-10-02T06:32:27.849653Z","updated_at":"2024-10-02T06:32:27.880935Z"}, {"id":35,"user":7,"location":"SRID=4326;POINT (-122.084 37.4219983)","title":"Discount Event","description":"SmartPhone","media":"http://7636-175-198-127-14.ngrok-free.app/media/pins_images/2024-10-03-06-19-40-857.jpg","is_ads":true,"info":"{\"range\":3,\"duration\":8,\"additionalInfo\":\"{\\\"field1\\\":\\\"Samsung\\\",\\\"field2\\\":\\\"250\\\",\\\"field3\\\":\\\"5%\\\"}\"}","tags":[{"id":2,"name":"전자기기"},{"id":6,"name":"베스트셀러"}],"created_at":"2024-10-03T06:20:21.709845Z","updated_at":"2024-10-03T06:20:21.768391Z"}]
"""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchBar = view.findViewById(R.id.search_bar)
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view)

        // 검색 결과 리스트 어댑터 초기화
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)
        pinAdapter = PinAdapter(searchResults) { pin ->
            showPinOnMap(pin)
        }
        searchResultsRecyclerView.adapter = pinAdapter

        // 검색바 입력 이벤트 처리
        searchBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()

                if (query.isNotEmpty()) {
                    searchResultsRecyclerView.visibility = View.VISIBLE
                    searchPins(query) // 서버로 검색 요청
                }
                true
            } else {
                false
            }
        }

        // 지도 프래그먼트 동적으로 추가
        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getCurrentLocation() // 현재 위치 가져오기

        return view
    }

    // 지도 준비 완료 시 호출되는 콜백
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        isMapReady = true

        // 지도 스타일 적용
        setMapStyle(map!!)

        map?.setOnMarkerClickListener { marker ->
            val pinData = marker.tag as? PinDataResponse
            Log.d("pinData", "$pinData")
            if (pinData != null) {
                val bottomSheet = PinDetailBottomSheet.newInstance(Gson().toJson(pinData))
                bottomSheet.show(parentFragmentManager, bottomSheet.tag)
            }
            true
        }

        // 서울 초기 위치 설정
        val seoul = LatLng(37.5665, 126.978) // 서울의 위도와 경도
        map?.addMarker(MarkerOptions().position(seoul).title("Marker in Seoul"))
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 10f))

        map?.uiSettings?.isZoomControlsEnabled = true // 확대/축소 버튼 활성화
        map?.uiSettings?.isZoomGesturesEnabled = true // 제스처로 확대/축소 가능

        getCurrentLocation()
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

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {

        if (!isMapReady || map == null) {
            Log.d("SearchFragment", "Map is not ready yet")
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(location.latitude, location.longitude)
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f))
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



    // 임의의 검색 결과 리스트를 반환하는 함수 (추후 데이터베이스와 연동 필요)
    @SuppressLint("NotifyDataSetChanged")
    private fun searchPins(query: String) {
        val userLatitude = userLocation?.latitude ?: 37.7749 // 사용자 위치의 위도
        val userLongitude = userLocation?.longitude ?: -122.4194 // 사용자 위치의 경도
        val searchRadius = 10000 // 10km 반경

        lifecycleScope.launch {
            try {
                // 핀 검색
                val response = RetrofitInstance.api.searchPins(query, userLatitude, userLongitude, searchRadius)
                Log.d("response", "$response")

                if (response.isSuccessful) {
                    val Data = jsonString //response.body()?.toString()
                    Log.d("PinData", "$Data")
                    if (Data.isNullOrEmpty()) {
                        Log.d("SearchFragment", "No pins received")
                        Toast.makeText(requireContext(), "No pins found", Toast.LENGTH_SHORT).show()
                        searchResultsRecyclerView.visibility = View.GONE
                    } else {
                        // JSON 배열을 수동으로 파싱
                        val jsonArray = JSONArray(Data)
                        Log.d("SearchFragment", "Parsed JSON array: $jsonArray")
                        searchResults.clear()

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            Log.d("SearchFragment", "Processing JSON object at index $i: $jsonObject")

                            val tagsArray = jsonObject.optJSONArray("tags")
                            val tagsList = mutableListOf<String>()
                            tagsArray?.let {
                                Log.d("SearchFragment", "Found tags array: $tagsArray")
                                for (j in 0 until it.length()) {
                                    val tagObject = it.getJSONObject(j)
                                    val tagName = tagObject.getString("name")
                                    Log.d("SearchFragment", "Found tag: $tagName")
                                    tagsList.add(tagName)
                                }
                            }

                            val title = jsonObject.optString("title", "제목 없음")
                            Log.d("SearchFragment", "Pin title: $title")
                            if (title.contains(
                                    query,
                                    ignoreCase = true
                                ) || tagsList.any { it.contains(query, ignoreCase = true) }
                            ) {
                                if (jsonObject.has("media")) {
                                    val media = jsonObject.getString("media")
                                    Log.d("SearchFragment", "Found media: $media")
                                    jsonObject.put(
                                        "media_files",
                                        JSONArray().put(media)
                                    ) // media_files로 추가
                                }

                                jsonObject.remove("tags")

                                val pinData = Gson().fromJson(
                                    jsonObject.toString(),
                                    PinDataResponse::class.java
                                )
                                Log.d("SearchFragment", "Parsed PinDataResponse: $pinData")
                                searchResults.add(pinData)
                                Log.d("SearchFragment", "Added pin data to searchResults, total count: ${searchResults.size}")
                                pinAdapter.notifyDataSetChanged() // 어댑터에 데이터 변경 알림
                                searchResultsRecyclerView.visibility = View.VISIBLE // 결과가 있으면 RecyclerView를 보이게 설정
                                Log.d("SearchFragment", "Search results count: ${searchResults.size}")
                            }
                        }
                    }
                } else {
                    Log.e("SearchFragment", "Failed to fetch search results: ${response.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "Error fetching pins: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SearchFragment", "Error during search", e)
                Toast.makeText(requireContext(), "Error during search: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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



    private fun showPinOnMap(pin: PinDataResponse) {
        // 핀 위치에서 위도와 경도 추출
        Log.d("Pin", "$pin")
        val locationStr = pin.location // 핀의 위치 문자열
        val regex = """POINT \((-?\d+\.?\d*) (-?\d+\.?\d*)\)""".toRegex()
        val matchResult = regex.find(locationStr)

        if (matchResult != null) {
            val (longitude, latitude) = matchResult.destructured
            val title = pin.title ?: "제목 없음"
            val description = pin.description ?: "설명 없음"
            val isAds = pin.is_ads
            val media_files = pin.media_files
            Log.d("pindata", "$title, $description, $isAds")
            Log.d("pindata", "Media Files: $media_files")

            val pinLocation = LatLng(latitude.toDouble(), longitude.toDouble())

            // 광고 여부에 따라 색상 설정
            val borderColor = if (isAds == true) {
                Color.parseColor("#C8E6C9") // 광고용 색상
            } else {
                Color.parseColor("#FFAB91") // 일반 핀 색상
            }

            // 마커 생성
            val markerIcon = createMarkerIcon(borderColor)
            val marker = map?.addMarker(
                MarkerOptions()
                    .position(pinLocation)
                    .title(title)
                    .snippet(description)
                    .icon(markerIcon)
            )
            marker?.tag = pin // 마커에 데이터 태그 추가 (PinDataResponse 객체)

            // 카메라를 해당 핀 위치로 이동
            map?.animateCamera(CameraUpdateFactory.newLatLng(pinLocation))
            searchResultsRecyclerView.visibility = View.GONE
        }
    }

}
