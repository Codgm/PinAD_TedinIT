package com.example.mappin_fe.AddPin.PointPay

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.mappin_fe.Login_Sign.UserAccount
import com.example.mappin_fe.MainActivity
import com.example.mappin_fe.R
import com.example.mappin_fe.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_point_system, container, false)
        receivedSubCategory = arguments?.getString("SELECTED_SUBCATEGORY") ?: ""

        tvCurrentPoints = view.findViewById(R.id.tv_current_points)
        tvEstimatedCost = view.findViewById(R.id.tv_estimated_cost)
        imgPointIcon = view.findViewById(R.id.img_point_icon)
        spinnerAdRange = view.findViewById(R.id.spinner_ad_range)
        spinnerAdDuration = view.findViewById(R.id.spinner_ad_duration)
        switchAdBoost = view.findViewById(R.id.switch_ad_boost)
        btnCompletePin = view.findViewById(R.id.btn_complete_pin)

        setupSpinners()
        updateCurrentPointsText()
        setupListeners()

        return view
    }

    private fun setupSpinners() {
        // 광고 범위 스피너 어댑터
        val rangeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ad_ranges,
            android.R.layout.simple_spinner_item
        )
        rangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAdRange.adapter = rangeAdapter

        // 광고 기간 스피너 어댑터
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
                calculateAndUpdatePoints()

                val selectedRange = spinnerAdRange.selectedItemPosition
                val selectedDuration = spinnerAdDuration.selectedItemPosition

                savePinData(
                    latitude = 37.7749,
                    longitude = -122.4194,
                    title = "$nickname 핀", // 사용자 닉네임을 포함한 제목
                    range = selectedRange,
                    duration = selectedDuration,
                    subCategory = receivedSubCategory
                )

                Toast.makeText(context, "Pin setup completed!", Toast.LENGTH_SHORT).show()

                // Navigate back to MainActivity
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                activity?.finish()
            }
        }
    }

    private fun calculateEstimatedCost() {
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
    }

    private fun calculateAndUpdatePoints() {
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

        if (currentPoints >= totalCost) {
            currentPoints -= totalCost
            updateCurrentPointsText()
        } else {
            Toast.makeText(context, "포인트가 부족합니다!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePinData(latitude: Double, longitude: Double, title: String, range: Int, duration: Int, subCategory: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("PinData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val pinData = "$latitude,$longitude,$title,$range,$duration,$subCategory"

        editor.putString("last_pin", pinData)
        editor.apply()
    }
}
