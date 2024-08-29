package com.example.mappin_fe.splash.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mappin_fe.R

class OnboardingFragment : Fragment() {
    private var pageNumber: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_onboarding_fragment, container, false)

        arguments?.let {
            pageNumber = it.getInt(ARG_PAGE_NUMBER)
        }

        val ivFeature: ImageView = view.findViewById(R.id.ivFeature)
        val tvFeatureTitle: TextView = view.findViewById(R.id.tvFeatureTitle)
        val tvFeatureDescription: TextView = view.findViewById(R.id.tvFeatureDescription)
        val tvPageIndicator: TextView = view.findViewById(R.id.tvPageIndicator)

        when (pageNumber) {
            0 -> {
                ivFeature.setImageResource(android.R.drawable.ic_dialog_map)
                tvFeatureTitle.text = "지도 기반 위치 공유"
                tvFeatureDescription.text = "지도와 연동된 위치 기반 정보 탐색 및 공유 기능을 제공합니다."
            }
            1 -> {
                ivFeature.setImageResource(android.R.drawable.ic_lock_lock)
                tvFeatureTitle.text = "편리한 사용자 인증"
                tvFeatureDescription.text = "Google 로그인으로 간편하게 계정을 생성하고, 개인화된 서비스를 경험하세요."
            }
            2 -> {
                ivFeature.setImageResource(android.R.drawable.ic_menu_edit)
                tvFeatureTitle.text = "스토리 작성 및 포인트 시스템"
                tvFeatureDescription.text = "사진과 텍스트로 나만의 이야기를 공유하고, 포인트로 더 많은 혜택을 누리세요."
            }
        }

        tvPageIndicator.text = "${pageNumber + 1} / 3"

        return view
    }

    companion object {
        private const val ARG_PAGE_NUMBER = "page_number"

        fun newInstance(pageNumber: Int): OnboardingFragment {
            val fragment = OnboardingFragment()
            Bundle().apply {
                putInt(ARG_PAGE_NUMBER, pageNumber)
                fragment.arguments = this
            }
            return fragment
        }
    }
}
