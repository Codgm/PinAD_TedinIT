package com.example.mappin_fe

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PinData(
    val title: String,
    val range: Int,
    val duration: Int,
    val mainCategory: String,
    val subCategory: String,
    val mediaUri: String,
    val contentData: String,
    val tags: List<String>
) : Parcelable {
    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        TODO("Not yet implemented")
    }
}

class PinDetailBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(pinData: PinData): PinDetailBottomSheet {
            return PinDetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable("PIN_DATA", pinData)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_pin_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pinData = arguments?.getParcelable<PinData>("PIN_DATA") ?: return

        view.findViewById<TextView>(R.id.tvTitle).text = pinData.title
        view.findViewById<TextView>(R.id.tvCategory).text = "${pinData.mainCategory} > ${pinData.subCategory}"
        view.findViewById<TextView>(R.id.tvRange).text = "Range: ${pinData.range} km"
        view.findViewById<TextView>(R.id.tvDuration).text = "Duration: ${pinData.duration} hours"

        // 구매 버튼 표시 여부 설정
        val btnPurchase = view.findViewById<Button>(R.id.btnPurchase)
        if (pinData.subCategory == "유통") {
            btnPurchase.visibility = View.VISIBLE
        } else {
            btnPurchase.visibility = View.GONE
        }

        // Add tags
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupTags)
        pinData.tags.forEach { tag ->
            val chip = Chip(context)
            chip.text = tag
            chipGroup.addView(chip)
        }

        // 타이머 설정 (초 단위)
        val chronometer = view.findViewById<Chronometer>(R.id.chronometer)
        val durationInMillis = pinData.duration * 60 * 60 * 1000L // 시간 단위를 밀리초로 변환
        chronometer.base = SystemClock.elapsedRealtime() + durationInMillis
        chronometer.start()

        // 진행 상황 ProgressBar 업데이트
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val maxParticipants = 100 // 최대 인원 (예시로 100명 설정)
        val currentParticipants = 50 // 현재 참여 인원 (예시로 50명 설정)
        progressBar.max = maxParticipants
        progressBar.progress = currentParticipants

        // 참여 인원 정보 업데이트
        view.findViewById<TextView>(R.id.tvParticipantInfo).text = "현재 참여: $currentParticipants / $maxParticipants"

        // 자세히 보기 버튼 클릭 시 콘텐츠와 미디어 보이기
        val tvMoreDetails = view.findViewById<TextView>(R.id.tvMoreDetails)
        val ivMedia = view.findViewById<ImageView>(R.id.ivMedia)
        val tvContent = view.findViewById<TextView>(R.id.tvContent)
        tvMoreDetails.setOnClickListener {
            ivMedia.load(pinData.mediaUri)
            ivMedia.visibility = View.VISIBLE
            tvContent.text = pinData.contentData
            tvContent.visibility = View.VISIBLE
        }
    }

}