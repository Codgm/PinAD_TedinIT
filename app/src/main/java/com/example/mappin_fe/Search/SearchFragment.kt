package com.example.mappin_fe.Search

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.mappin_fe.Data.PinDataResponse
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment(), OnMapReadyCallback {

    private lateinit var searchBar: EditText
    private lateinit var searchResultsList: ListView
    private lateinit var searchResultsAdapter: ArrayAdapter<String>
    private val searchHistory = mutableListOf<String>()
    private val searchResults = mutableListOf<PinDataResponse>()

    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchBar = view.findViewById(R.id.search_bar)
        searchResultsList = view.findViewById(R.id.search_results_list)

        // 검색 결과 리스트 어댑터 초기화
        searchResultsAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            mutableListOf<String>() // 어댑터는 String 리스트로 초기화
        )
        searchResultsList.adapter = searchResultsAdapter

        // 검색바 입력 이벤트 처리
        searchBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()

                if (query.isNotEmpty()) {
                    searchResultsList.visibility = View.VISIBLE
                    searchPins(query) // 서버로 검색 요청
                }
                true
            } else {
                false
            }
        }

        searchResultsList.setOnItemClickListener { _, _, position, _ ->
            val selectedResult = searchResults[position]
            showPinOnMap(selectedResult) // 핀을 지도에 표시하는 함수 호출
        }

        // 지도 프래그먼트 동적으로 추가
        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)

        return view
    }

    // 지도 준비 완료 시 호출되는 콜백
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // 지도 스타일 적용
        setMapStyle(map)

        // 서울 초기 위치 설정
        val seoul = LatLng(37.5665, 126.978) // 서울의 위도와 경도
        map.addMarker(MarkerOptions().position(seoul).title("Marker in Seoul"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 10f))
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


    // 임의의 검색 결과 리스트를 반환하는 함수 (추후 데이터베이스와 연동 필요)
    private fun searchPins(query: String) {
        val userLatitude = 37.7749 // 사용자의 위도, 실제로는 현재 위치를 가져와야 함
        val userLongitude = -122.4194 // 사용자의 경도, 실제로는 현재 위치를 가져와야 함
        val searchRadius = 1000 // 검색 반경 (미터 단위)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 서버에 검색 요청 보내기
                val response = RetrofitInstance.api.searchPins(query, userLatitude, userLongitude, searchRadius)

                if (response.isSuccessful) {
                    val searchResultsFromServer = response.body() ?: emptyList()

                    // 쿼리를 title 및 tags로 필터링
                    val filteredResults = searchResultsFromServer.filter { pin ->
                        pin.title.contains(query, ignoreCase = true) ||
                                pin.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                    }

                    // UI 스레드에서 결과 업데이트
                    withContext(Dispatchers.Main) {
                        searchResults.clear()
                        searchResults.addAll(filteredResults)

                        // 어댑터에 title만 추가
                        val titles = filteredResults.map { it.title }
                        searchResultsAdapter.clear()
                        searchResultsAdapter.addAll(titles) // 타이틀 리스트를 어댑터에 추가
                        searchResultsAdapter.notifyDataSetChanged() // 리스트 업데이트
                    }
                } else {
                    Log.e("SearchFragment", "Failed to fetch search results: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("SearchFragment", "Error during search", e)
            }
        }
    }


    private fun showPinOnMap(pinData: PinDataResponse) {
        val pinLocation = LatLng(pinData.latitude, pinData.longitude) // 서버로부터 받은 좌표 사용
        map.clear() // 현재 지도에서 모든 마커 제거
        map.addMarker(MarkerOptions().position(pinLocation).title(pinData.title))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pinLocation, 15f))

        searchResultsList.visibility = View.GONE
    }
}
