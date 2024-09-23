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
        view.findViewById<TextView>(R.id.tvCategory).text = "${pinData.mainCategory} > ${pinData.subCategory}"
        view.findViewById<TextView>(R.id.tvRange).text = "Range: ${pinData.range} km"
        view.findViewById<TextView>(R.id.tvDuration).text = "Duration: ${pinData.duration} hours"

        setupPurchaseButton(view, pinData)
        setupTags(view, pinData)
        setupChronometer(view, pinData)
        setupProgressBar(view)
        setupMoreDetailsButton(view, pinData)
    }

    private fun setupPurchaseButton(view: View, pinData: PinDataResponse) {
        val btnPurchase = view.findViewById<Button>(R.id.btnPurchase)
        btnPurchase.visibility = if (pinData.subCategory == "유통") View.VISIBLE else View.GONE
    }

    private fun setupTags(view: View, pinData: PinDataResponse) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupTags)
        pinData.tags.forEach { tag ->
            val chip = Chip(context)
            chip.text = tag
            chipGroup.addView(chip)
        }
    }

    private fun setupChronometer(view: View, pinData: PinDataResponse) {
        val chronometer = view.findViewById<Chronometer>(R.id.chronometer)
        val remainingTime = pinData.created_at.time - Date().time
        chronometer.base = SystemClock.elapsedRealtime() + remainingTime
        chronometer.start()
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
            ivMedia.load(pinData.media_files)
            ivMedia.visibility = View.VISIBLE
            tvContent.text = pinData.info.toString()
            tvContent.visibility = View.VISIBLE
        }
    }
}
