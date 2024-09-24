package com.example.mappin_fe

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import coil.load
import com.example.mappin_fe.Data.PinDataResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import org.json.JSONObject
import java.util.*

class PinDetailBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(pinDataJson: String): PinDetailBottomSheet {
            return PinDetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("PIN_DATA_JSON", pinDataJson)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_pin_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val pinDataJson = arguments?.getString("PIN_DATA_JSON") ?: return
            val gson = Gson()
            val pinData = gson.fromJson(pinDataJson, PinDataResponse::class.java)

            setupUI(view, pinData)
        } catch (e: Exception) {
            // Handle error (e.g., show error message)
        }
    }

    private fun setupUI(view: View, pinData: PinDataResponse) {
        view.findViewById<TextView>(R.id.tvTitle).text = pinData.title
        view.findViewById<TextView>(R.id.tvDescription).text = "${pinData.description}"

        val infoJson = JSONObject(pinData.info.toString())
        val range = infoJson.optInt("range", 0)
        val duration = infoJson.optInt("duration", 0)

        view.findViewById<TextView>(R.id.tvRange).text = "Range: $range km"
        view.findViewById<TextView>(R.id.tvDuration).text = "Duration: $duration hours"

        setupPurchaseButton(view, pinData)
        setupTags(view, pinData)
        setupChronometer(view, pinData, duration)
        setupProgressBar(view)
        setupMoreDetailsButton(view, pinData)
    }

    private fun setupPurchaseButton(view: View, pinData: PinDataResponse) {
        val btnPurchase = view.findViewById<Button>(R.id.btnPurchase)
        btnPurchase.visibility = if (pinData.category == "광고") View.VISIBLE else View.GONE
    }

    private fun setupTags(view: View, pinData: PinDataResponse) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupTags)
        pinData.tags.forEach { tag ->
            val chip = Chip(context)
            chip.text = tag
            chipGroup.addView(chip)
        }
    }

    private fun setupChronometer(view: View, pinData: PinDataResponse, duration: Int) {
        val chronometer = view.findViewById<Chronometer>(R.id.chronometer)
        val endTime = pinData.created_at.time + (duration * 60 * 60 * 1000)
        val remainingTime = endTime - System.currentTimeMillis()
        if (remainingTime > 0) {
            chronometer.base = SystemClock.elapsedRealtime() + remainingTime
            chronometer.start()
        } else {
            chronometer.text = "Expired"
        }
    }

    private fun setupProgressBar(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val maxParticipants = 100 // Example value
        val currentParticipants = 50 // Example value
        progressBar.max = maxParticipants
        progressBar.progress = currentParticipants
        view.findViewById<TextView>(R.id.tvParticipantInfo).text = "Current participants: $currentParticipants / $maxParticipants"
    }

    private fun setupMoreDetailsButton(view: View, pinData: PinDataResponse) {
        val tvMoreDetails = view.findViewById<TextView>(R.id.tvMoreDetails)
        val ivMedia = view.findViewById<ImageView>(R.id.ivMedia)
        val tvContent = view.findViewById<TextView>(R.id.tvContent)

        tvMoreDetails.setOnClickListener {
            if (pinData.media_files.isNotEmpty()) {
                ivMedia.load(pinData.media_files[0])
                ivMedia.visibility = View.VISIBLE
            } else {
                ivMedia.visibility = View.GONE
            }

            val infoJson = JSONObject(pinData.info.toString())
            val additionalInfo = infoJson.optString("additionalInfo", "No additional information")
            tvContent.text = additionalInfo
            tvContent.visibility = View.VISIBLE

            // 토글 기능 추가
            if (tvMoreDetails.text == "More Details") {
                tvMoreDetails.text = "Less Details"
            } else {
                tvMoreDetails.text = "More Details"
                ivMedia.visibility = View.GONE
                tvContent.visibility = View.GONE
            }
        }
    }
}
