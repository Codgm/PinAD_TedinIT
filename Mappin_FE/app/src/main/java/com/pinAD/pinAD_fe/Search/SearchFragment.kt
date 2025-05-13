package com.pinAD.pinAD_fe.Search

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
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
import com.bumptech.glide.Glide
import com.pinAD.pinAD_fe.Data.pin.FltPinData
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.Data.pin.Tag
import com.pinAD.pinAD_fe.Data.TagsDeserializer
import com.pinAD.pinAD_fe.PinDetailBottomSheet
import com.pinAD.pinAD_fe.R
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
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pinAD.pinAD_fe.network.UserDataManager
import kotlinx.coroutines.launch

class SearchFragment : Fragment(), OnMapReadyCallback {

    private lateinit var searchBar: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var pinAdapter: PinAdapter
    private val searchHistory = mutableListOf<String>()
    private val searchResults = mutableListOf<FltPinData>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: LatLng? = null

    private var map: GoogleMap? = null
    private var isMapReady = false
    private lateinit var searchQuery: String
    private var isTagSearch: Boolean = false
    private var currentPage = 1
    private var isLoading = false
    private var hasMoreData = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchQuery =""

        searchBar = view.findViewById(R.id.search_bar)
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view)

        // 검색 결과 리스트 어댑터 초기화
        pinAdapter = PinAdapter(
            searchResults,
            { pin -> showPinOnMap(pin) },
            searchQuery,
            isTagSearch,
        )
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)
        searchResultsRecyclerView.adapter = pinAdapter
//        pinAdapter = PinAdapter(searchResults) { pin ->
//            showPinOnMap(pin)
//        }
//        searchResultsRecyclerView.adapter = pinAdapter

        // 검색바 입력 이벤트 처리
        searchBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchQuery = v.text.toString().trim()
                isTagSearch = searchQuery.startsWith("#")

                if (searchQuery.isNotEmpty()) {
                    searchResultsRecyclerView.visibility = View.VISIBLE
                    if (isTagSearch) {
                        searchTags(searchQuery) // 태그 검색 요청
                    } else {
                        searchPins(searchQuery) // 일반 핀 검색 요청
                    }
                }
                true
            } else {
                false
            }
        }

        setupScrollListener()

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

    private val estimatedItemHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.search_result_item_height) // dimens.xml에 정의 필요
    }

    private fun calculatePageSize(): Int {
        val layoutManager = searchResultsRecyclerView.layoutManager as LinearLayoutManager
        val viewportHeight = searchResultsRecyclerView.height
        val rowsPerScreen = viewportHeight / estimatedItemHeight
        return (rowsPerScreen + 1) // 화면에 보이는 행 수 + 1행
    }

    private fun loadMorePins() {
        if (!isLoading && hasMoreData) {
            currentPage++
            searchPins(searchQuery, isTagSearch = isTagSearch, loadMore = true)
        }
    }

    private fun setupScrollListener() {
        searchResultsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // 화면의 마지막 아이템에 도달하기 전에 다음 페이지 로드
                if (!isLoading && hasMoreData) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3
                        && firstVisibleItemPosition >= 0
                    ) {
                        loadMorePins()
                    }
                }
            }
        })
    }

    private fun resetPagination() {
        currentPage = 1
        hasMoreData = true
        searchResults.clear()
        pinAdapter.notifyDataSetChanged()
    }

    // 지도 준비 완료 시 호출되는 콜백
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        isMapReady = true

        map?.setOnMarkerClickListener { marker ->
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

        // 서울 초기 위치 설정
        val seoul = LatLng(37.5665, 126.978) // 서울의 위도와 경도
        map?.addMarker(MarkerOptions().position(seoul).title("Marker in Seoul"))
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 10f))

        map?.uiSettings?.isZoomControlsEnabled = true // 확대/축소 버튼 활성화
        map?.uiSettings?.isZoomGesturesEnabled = true // 제스처로 확대/축소 가능

        getCurrentLocation()
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

    private fun searchTags(tag: String) {
        lifecycleScope.launch {
            try {
                val cleanTag = tag.substring(1)
                val response = RetrofitInstance.api.searchTags(cleanTag)

                if (response.isSuccessful()) {
                    val jsonResponse = response.body()
                    if (jsonResponse != null) {
                        // JSON 문자열을 TagResponse 객체로 변환
                        Log.d("TAG", "Parsed Tags: $jsonResponse")
//                        val tagList: List<Tag> = gsonForTags.fromJson(jsonResponse, object : TypeToken<List<Tag>>() {}.type)
                        // RecyclerView에 데이터를 표시하는 로직 추가
//                        Log.d("TAG", "Parsed Tags: ${jsonResponse.tags}")
                        displayTagSearchResults(jsonResponse.tags)  // List<Tag>로 넘겨줍니다.
                    } else {
                        Log.e("TAG", "Response body is null")
                    }
                } else {
//                    Log.e("TAG", "Error response: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("SearchFragment", "Error during tag search", e)
                Toast.makeText(requireContext(), "Error during tag search: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayTagSearchResults(tagSearchResponse: List<Tag>) {
        // 태그 결과를 RecyclerView로 표시하는 로직
        val tagAdapter = TagAdapter(tagSearchResponse, object : TagAdapter.OnItemClickListener {
            override fun onItemClick(tag: Tag) {
                // 클릭한 태그로 핀 검색
                Log.d("tagName", "${tag.name}")
                searchPins(tag.name, isTagSearch = true) // '#' 제거 후 검색
            }
        })
        searchResultsRecyclerView.adapter = tagAdapter
        searchResultsRecyclerView.visibility = View.VISIBLE
    }





    // 임의의 검색 결과 리스트를 반환하는 함수 (추후 데이터베이스와 연동 필요)
    @SuppressLint("NotifyDataSetChanged")
    private fun searchPins(query: String, isTagSearch: Boolean = false, loadMore: Boolean = false) {
        if (!loadMore) {
            resetPagination()
        }

        isLoading = true
        val userLatitude = userLocation?.latitude ?: 37.7749
        val userLongitude = userLocation?.longitude ?: -122.4194
        val searchRadius = UserDataManager.userData?.radius
        val dynamicPageSize = calculatePageSize()

        lifecycleScope.launch {
            try {
                val response = searchRadius?.let {
                    RetrofitInstance.api.searchPins(
                        query,
                        userLatitude,
                        userLongitude,
                        it,
                        page_size = dynamicPageSize // 동적으로 계산된 페이지 크기 사용
                    )
                }

                if (response?.isSuccessful == true) {
                    val paginatedResponse = response.body()
                    val pinDataList = paginatedResponse?.results

                    hasMoreData = paginatedResponse?.next != null

                    if (pinDataList.isNullOrEmpty()) {
                        if (!loadMore) {
                            Toast.makeText(requireContext(), "No pins found", Toast.LENGTH_SHORT).show()
                            searchResultsRecyclerView.visibility = View.GONE
                        }
                    } else {
                        val filteredPins = pinDataList.filter { pin ->
                            if (isTagSearch) {
                                pin.tags.any { it.name.equals(query, ignoreCase = true) }
                            } else {
                                pin.tags.any { it.name.equals(query, ignoreCase = true) } ||
                                        pin.title.contains(query, ignoreCase = true)
                            }
                        }

                        if (loadMore) {
                            val oldSize = searchResults.size
                            searchResults.addAll(filteredPins)
                            pinAdapter.notifyItemRangeInserted(oldSize, filteredPins.size)
                        } else {
                            searchResults.clear()
                            searchResults.addAll(filteredPins)
                            pinAdapter.notifyDataSetChanged()
                        }

                        searchResultsRecyclerView.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("SearchFragment", "Error: ${response?.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "Error loading pins", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SearchFragment", "Error during search", e)
                Toast.makeText(requireContext(), "Error during search: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
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



    private fun showPinOnMap(pin: FltPinData) {
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
            val media_files = pin.media
            val profile_picture = pin.profile_picture
            Log.d("pindata", "$title, $description, $isAds")
            Log.d("pindata", "Media Files: $media_files")

            val pinLocation = LatLng(latitude.toDouble(), longitude.toDouble())

            // 광고 여부에 따라 색상 설정
            val borderColor = when(pin.pin_type) {
                1 -> {Color.parseColor("#F44336")}
                2 -> {Color.parseColor("#9C27B0")}
                else -> {Color.parseColor("#388E3C")}
            }

            // 마커 생성
            val markerIcon = createMarkerIcon(borderColor, profile_picture)
            val marker = map?.addMarker(
                MarkerOptions()
                    .position(pinLocation)
                    .title(title)
                    .snippet(description)
                    .icon(markerIcon)
            )
//            val pinJson = Gson().toJson(pin)
            marker?.tag = pin.id// 마커에 데이터 태그 추가 (PinDataResponse 객체)

            // 카메라를 해당 핀 위치로 이동
            map?.animateCamera(CameraUpdateFactory.newLatLng(pinLocation))
            searchResultsRecyclerView.visibility = View.GONE
        }
    }

}
