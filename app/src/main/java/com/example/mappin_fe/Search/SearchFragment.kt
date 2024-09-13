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
import com.example.mappin_fe.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class SearchFragment : Fragment(), OnMapReadyCallback {

    private lateinit var searchBar: EditText
    private lateinit var searchResultsList: ListView
    private lateinit var searchResultsAdapter: ArrayAdapter<String>
    private val searchHistory = mutableListOf<String>()
    private val searchResults = mutableListOf<String>()

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
            searchResults
        )
        searchResultsList.adapter = searchResultsAdapter

        // 검색바 입력 이벤트 처리
        searchBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // 서치 버튼 클릭 시 결과 리스트 업데이트
                searchResultsList.visibility = View.VISIBLE
                searchResults.clear()
                searchResults.addAll(searchPins(v.text.toString().trim())) // 검색 결과 추가
                searchResultsAdapter.notifyDataSetChanged()
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
    private fun searchPins(query: String): List<String> {
        // 여기에 데이터베이스에서 핀 검색 로직을 구현
        return listOf("Pin 1", "Pin 2", "Pin 3").filter {
            it.contains(query, ignoreCase = true)
        }
    }

    private fun showPinOnMap(pin: String) {
        // 여기에 핀의 위치를 좌표로 변환하여 지도에 마커를 추가합니다.
        val pinLocation = LatLng(37.7749, -122.4194) // 예시 위치를 사용하세요
        map.clear() // 현재 지도에서 모든 마커 제거
        map.addMarker(MarkerOptions().position(pinLocation).title(pin))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pinLocation, 15f))

        // 결과 리스트 화면을 숨깁니다.
        searchResultsList.visibility = View.GONE
    }
}
