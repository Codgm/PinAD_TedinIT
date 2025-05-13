package com.pinAD.pinAD_fe.splash.screen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.pinAD.pinAD_fe.R

class OnboardingFragment : Fragment() {

    private var pageNumber: Int = 0

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_onboarding, container, false)

        arguments?.let {
            pageNumber = it.getInt(ARG_PAGE_NUMBER)
        }

        val ivFeature: ImageView = view.findViewById(R.id.ivFeature)
        val tvFeatureTitle: TextView = view.findViewById(R.id.tvFeatureTitle)
        val tvFeatureDescription: TextView = view.findViewById(R.id.tvFeatureDescription)
        val tvPageIndicator: TextView = view.findViewById(R.id.tvPageIndicator)

        when (pageNumber) {
            0 -> {
                ivFeature.setImageResource(R.drawable.ic_ad_creation)
                tvFeatureTitle.text = "광고 핀 작성 및 쿠폰 발행\n Create advertising pins and issue coupons"
                tvFeatureDescription.text = "광고주는 핀을 통해 자신을 홍보하고, 쿠폰을 발행해 고객 유입을 유도할 수 있습니다.\n Advertisers can promote themselves through pins and attract customers by issuing coupons."
            }
            1 -> {
                ivFeature.setImageResource(R.drawable.ic_coupon_request)
                tvFeatureTitle.text = "고객의 쿠폰 요청 기능\n Customer's coupon request feature"
                tvFeatureDescription.text = "고객은 원하는 할인율과 내용을 작성하여 광고주에게 쿠폰을 요청할 수 있습니다 (단체 예약,대량 구매에 유용 합니다).\n Customers can request coupons from advertisers by filling out the discount rate and content they want (useful for group reservations, bulk purchases)."
            }
            2 -> {
                ivFeature.setImageResource(R.drawable.ic_social_network)
                tvFeatureTitle.text = "소셜 네트워크와 리뷰 작성\n Create social networks and reviews"
                tvFeatureDescription.text = "핀 리뷰 작성으로 포인트를 획득하고, 광고주와 고객 간 소셜 네트워킹을 활성화합니다.\n Earn points by writing pin reviews and enable social networking between advertisers and customers."
            }
            3 -> {
                ivFeature.setImageResource(R.drawable.ic_point_system)
                tvFeatureTitle.text = "포인트 기반 핀 작성\n Create a point-based pin"
                tvFeatureDescription.text = "포인트를 사용하여 핀을 작성하고, 더 많은 기능을 이용해 나만의 이야기를 공유해보세요.\n Use your points to create pins, and share your own stories with more features."
            }
            4 -> {
                ivFeature.setImageResource(R.drawable.ic_pin_search)
                tvFeatureTitle.text = "핀 서치 및 핀스토리 탐색\n Find and explore pinstories"
                tvFeatureDescription.text = "광고와 사용자 핀스토리를 탐색하며, 광고주와 고객 간의 네트워크를 형성하세요.\n Explore advertising and user pinstories, and build a network between advertisers and customers."
            }
        }

        tvPageIndicator.text = "${pageNumber + 1} / 5"
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
