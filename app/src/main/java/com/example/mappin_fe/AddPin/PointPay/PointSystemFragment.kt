package com.example.mappin_fe.AddPin.PointPay

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mappin_fe.Data.PinDataResponse
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.MainActivity
import com.example.mappin_fe.R
import com.example.mappin_fe.UserUtils
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PointSystemFragment : Fragment() {

    private lateinit var tvCurrentPoints: TextView
    private lateinit var tvEstimatedCost: TextView
    private lateinit var imgPointIcon: ImageView
    private lateinit var btnCompletePin: Button
    private lateinit var spinnerAdRange: Spinner
    private lateinit var spinnerAdDuration: Spinner
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchAdBoost: Switch
    private var currentPoints = 1000
    private lateinit var receivedSubCategory: String
    private lateinit var media: String
    private lateinit var receivedMainCategory: String
    private lateinit var contentData: String
    private lateinit var selectedTags: List<String>
    private lateinit var switchVisibility: Switch
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_point_system, container, false)
        initializeViews(view)
        setupSpinners()
        updateCurrentPointsText()
        setupListeners()
        return view
    }

    private fun initializeViews(view: View) {
        receivedSubCategory = arguments?.getString("SELECTED_SUBCATEGORY") ?: ""
        receivedMainCategory = arguments?.getString("SELECTED_MAIN_CATEGORY") ?: ""
        contentData = arguments?.getString("CONTENT_DATA") ?: ""
        Log.d("PointSystemFragment", "Received Main Category: $receivedMainCategory")
        Log.d("PointSystemFragment", "Received Sub Category: $receivedSubCategory")
        Log.d("PointSystemFragment", "Content Data: $contentData")
//        media = arguments?.getString("MEDIA_URI") ?: ""
        tvCurrentPoints = view.findViewById(R.id.tv_current_points)
        tvEstimatedCost = view.findViewById(R.id.tv_estimated_cost)
        imgPointIcon = view.findViewById(R.id.img_point_icon)
        spinnerAdRange = view.findViewById(R.id.spinner_ad_range)
        spinnerAdDuration = view.findViewById(R.id.spinner_ad_duration)
        switchAdBoost = view.findViewById(R.id.switch_ad_boost)
        btnCompletePin = view.findViewById(R.id.btn_complete_pin)
        selectedTags = arguments?.getStringArray("SELECTED_TAGS")?.toList() ?: emptyList()
        switchVisibility = view.findViewById(R.id.switch_visibility)
    }

    private fun setupSpinners() {
        val rangeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ad_ranges,
            android.R.layout.simple_spinner_item
        )
        rangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAdRange.adapter = rangeAdapter

        val durationAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ad_durations,
            android.R.layout.simple_spinner_item
        )
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAdDuration.adapter = durationAdapter
    }

    private fun updateCurrentPointsText() {
        tvCurrentPoints.text = "Current Points: $currentPoints"
        imgPointIcon.setImageResource(R.drawable.ic_point)
    }

    private fun setupListeners() {
        spinnerAdRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                calculateEstimatedCost()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerAdDuration.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                calculateEstimatedCost()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        switchAdBoost.setOnCheckedChangeListener { _, _ ->
            calculateEstimatedCost()
        }

        btnCompletePin.setOnClickListener {
            UserUtils.fetchUserDetails { nickname, _ ->
                if (calculateAndUpdatePoints()) {
                    val pinData = createPinData(nickname)
                    sendPinDataToServer(pinData)
                    navigateToMainActivity()
                } else {
                    Toast.makeText(context, "Insufficient points!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun calculateEstimatedCost(): Int {
        val rangeCost = when (spinnerAdRange.selectedItemPosition) {
            1 -> 50
            2 -> 100
            else -> 0
        }

        val durationCost = when (spinnerAdDuration.selectedItemPosition) {
            1 -> 100
            2 -> 200
            else -> 0
        }

        val boostCost = if (switchAdBoost.isChecked) 50 else 0

        val totalCost = rangeCost + durationCost + boostCost
        tvEstimatedCost.text = "Estimated Cost: $totalCost"
        return totalCost
    }

    private fun calculateAndUpdatePoints(): Boolean {
        val totalCost = calculateEstimatedCost()
        return if (currentPoints >= totalCost) {
            currentPoints -= totalCost
            updateCurrentPointsText()
            true
        } else {
            false
        }
    }

    private fun createPinData(nickname: String): PinDataResponse {
        val now = Date()
        val location = LatLng(latitude,longitude)
//        val mediaList = listOf(media)
        return PinDataResponse(
            id = UUID.randomUUID().toString(),
            latitude = 37.7749,
            longitude = -122.4194,
            location = location.toString(),
            user = nickname.toIntOrNull() ?: 0,
            title = "$nickname 핀",
            description = "Your description here",
            range = getSelectedRange(),
            duration = getSelectedDuration(),
            mainCategory = receivedMainCategory,
            subCategory = receivedSubCategory,
//            media = mediaList,
            contentData = contentData,
            tags = selectedTags,
            visibility = if (switchVisibility.isChecked) "public" else "private",
            created_at = now,
            updated_at = Date(now.time + (getSelectedDuration() * 60 * 60 * 1000))
        )
    }

    private fun sendPinDataToServer(pinData: PinDataResponse) {
        lifecycleScope.launch {
            Log.d("PinData", "Pin data to be sent: ${Gson().toJson(pinData)}")
            try {
                val response = RetrofitInstance.api.savePinData(pinData)
                if (response.isSuccessful) {
                    Log.d("PinData", "Pin data saved successfully: ${response.body()}")
                    navigateToMainActivity()
                } else {
                    Log.e("PinData", "Error saving pin data: HTTP ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Error saving pin data: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PinData", "Exception saving pin data", e)
                Toast.makeText(context, "Exception saving pin data", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun getSelectedRange(): Int = when (spinnerAdRange.selectedItemPosition) {
        0 -> 1
        1 -> 2
        2 -> 3
        else -> 0
    }

    private fun getSelectedDuration(): Int = when (spinnerAdDuration.selectedItemPosition) {
        0 -> 2
        1 -> 4
        2 -> 8
        else -> 0
    }
}