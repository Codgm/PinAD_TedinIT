package com.pinAD.pinAD_fe.HotPin

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.pinAD.pinAD_fe.Data.pin.FltPinData
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.Data.pin.Tag
import com.pinAD.pinAD_fe.R
import kotlinx.coroutines.launch

class HotPinFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tagRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var pinAdapter: HotPinAdapter
    private lateinit var tagAdapter: DefaultTagAdapter
    private lateinit var locationManager: LocationManager
    private var currentLocation: Location? = null
    private lateinit var pinCountTextView: TextView


    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hot_pin, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewHotPins)
        tagRecyclerView = view.findViewById(R.id.recyclerViewTags)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        pinCountTextView = view.findViewById(R.id.pinCountTextView)


        setupRecyclerView()
        setupTagRecyclerView()
        setupSwipeRefresh()
        setupLocation()
        setupDefaultTags()

        return view

    }

    private fun setupDefaultTags() {
        // 기본 태그 하드코딩
        val defaultTags = listOf(getString(R.string.tag_food_drink),
            getString(R.string.tag_clothing_shoes),
            getString(R.string.tag_beauty_health),
            getString(R.string.tag_sports_leisure),
            getString(R.string.tag_convenience_mart),
            getString(R.string.tag_culture_entertainment),
            getString(R.string.tag_education_books),
            getString(R.string.tag_automotive),
            getString(R.string.tag_life_service),
            getString(R.string.tag_event),
            getString(R.string.tag_other)) // 기본 태그 예시
//        updateChipGroup(defaultTags)
        tagAdapter.submitList(defaultTags)
    }

    private fun setupLocation() {
        val locationManager = requireContext().getSystemService(LocationManager::class.java)

        // 권한 이름을 문자열로 정의
        val fineLocationPermission = "android.permission.ACCESS_FINE_LOCATION"

        // 권한이 있는지 체크
        if (ActivityCompat.checkSelfPermission(requireContext(), fineLocationPermission) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(fineLocationPermission),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // 권한이 있는 경우 위치 정보 가져오기
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

    private fun setupTagRecyclerView() {
        tagAdapter = DefaultTagAdapter { selectedTag ->
            fetchRelatedTags(selectedTag)
        }
        tagRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = tagAdapter
        }
    }


    private fun setupRecyclerView() {
        pinAdapter = HotPinAdapter()
        recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = pinAdapter
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            fetchPins(tagAdapter.getSelectedTag())
        }
    }

//    private fun updateChipGroup(tags: List<String>) {
//        chipGroup.removeAllViews()
//        tags.forEach { tag ->
//            val chip = Chip(context).apply {
//                text = tag
//                isCheckable = true
//                setOnCheckedChangeListener { _, isChecked ->
//                    if (isChecked) {
//                        fetchRelatedTags(tag)
//                    } else if (chipGroup.checkedChipId == View.NO_ID) {
//                        fetchPins(null)
//                    }
//                }
//            }
//            chipGroup.addView(chip)
//        }
//    }

    private fun fetchRelatedTags(tagName: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.searchTags(keyword = tagName)
                if (response.isSuccessful) {
                    val relatedTags = response.body()?.tags ?: emptyList()
                    Log.d("relatedTag", "$tagName")
                    Log.d("relatedTag", "$relatedTags")
                    updatePinCount(relatedTags) // 게시물 수 업데이트
                    fetchPins(tagName) // 선택한 태그로 핀 가져오기
                } else {
                    // 에러 처리
                }
            } catch (e: Exception) {
                // 예외 처리
            }
        }
    }

    private fun updatePinCount(relatedTags: List<Tag>) {
        val totalCount = relatedTags.sumOf { it.post_count } // 모든 게시물 수 총합
        pinCountTextView.text = "$totalCount 개의 게시물" // TextView에 게시물 수 업데이트
    }

    private fun fetchPins(tagName: String?) {
        lifecycleScope.launch {
            swipeRefreshLayout.isRefreshing = true
            try {
                val response = RetrofitInstance.api.searchPins(
                    keyword = tagName,
                    latitude = currentLocation?.latitude ?: 0.0,
                    longitude = currentLocation?.longitude ?: 0.0,
                    radius = 1 // 예시 반경 (미터 단위)
                )
                if (response.isSuccessful) {
                    val pins: List<FltPinData> = response.body() ?: emptyList()
                    pinAdapter.submitList(pins)
                } else {
                    // 에러 처리
                }
            } catch (e: Exception) {
                // 예외 처리
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}