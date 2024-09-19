package com.example.mappin_fe.AddPin.Category

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.example.mappin_fe.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject

class CategorySelectionFragment : Fragment() {

    private lateinit var mainCategoryChipGroup: ChipGroup
    private lateinit var subCategoryChipGroup: ChipGroup
    private lateinit var templateCard: MaterialCardView
    private lateinit var templateTitle: TextView
    private lateinit var templateContent: LinearLayout
    private lateinit var nextButton: Button
    private var selectedSubCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category_selection, container, false)

        mainCategoryChipGroup = view.findViewById(R.id.mainCategoryChipGroup)
        subCategoryChipGroup = view.findViewById(R.id.subCategoryChipGroup)
        templateCard = view.findViewById(R.id.templateCard)
        templateTitle = view.findViewById(R.id.templateTitle)
        templateContent = view.findViewById(R.id.templateContent)
        nextButton = view.findViewById(R.id.btn_next)

        setupMainCategories()
        setupNextButton()

        return view
    }

    private fun setupNextButton() {
        nextButton.setOnClickListener {
            if (areAllFieldsFilled()) {
                // 선택한 서브카테고리 정보를 번들에 담아 전달
                val content = gatherContentData()
                val selectedMainChip = mainCategoryChipGroup.findViewById<Chip>(mainCategoryChipGroup.checkedChipId)
                val selectedSubChip = subCategoryChipGroup.findViewById<Chip>(subCategoryChipGroup.checkedChipId)

                val bundle = Bundle().apply {
                    putString("SELECTED_MAIN_CATEGORY", selectedMainChip?.text.toString())
                    putString("SELECTED_SUBCATEGORY", selectedSubChip?.text.toString())
                    putString("CONTENT_DATA", content)
                }
                val categoryTagFragment = CategoryTagFragment().apply {
                    arguments = bundle
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, categoryTagFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(context, "Please complete all required fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun gatherContentData(): String {
        val contentBuilder = StringBuilder()
        for (i in 0 until templateContent.childCount) {
            val view = templateContent.getChildAt(i)
            if (view is TextInputLayout) {
                val editText = view.getChildAt(0) as? TextInputEditText
                contentBuilder.append(editText?.text.toString()).append("\n")
            } else if (view is RatingBar) {
                contentBuilder.append("평점: ${view.rating}\n")
            }
        }
        return contentBuilder.toString()
    }




    private fun areAllFieldsFilled(): Boolean {
        return mainCategoryChipGroup.checkedChipId != -1 && templateContent.childCount > 0
    }

    private fun setupMainCategories() {
        val categories = listOf("광고", "핀스토리", "할인 요청")
        categories.forEach { category ->
            val chip = Chip(context)
            chip.text = category
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onMainCategorySelected(category)
                }
            }
            mainCategoryChipGroup.addView(chip)
        }
    }

    private fun onMainCategorySelected(category: String) {
        subCategoryChipGroup.removeAllViews()
        templateContent.removeAllViews()

        when (category) {
            "광고" -> setupSubCategories(listOf("유통", "F&B", "행사 알림"))
            "핀스토리" -> setupSubCategories(listOf("리뷰", "명소 추천", "약속 장소", "여행 메모"))
            "할인 요청" -> showTemplate("할인 요청")
        }
    }

    private fun setupSubCategories(subCategories: List<String>) {
        subCategories.forEach { subCategory ->
            val chip = Chip(context)
            chip.text = subCategory
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedSubCategory = subCategory
                    showTemplate(subCategory)
                }
            }
            subCategoryChipGroup.addView(chip)
        }
    }

    private fun showTemplate(type: String) {
        templateContent.removeAllViews()
        templateTitle.text = type

        val styleResId = getStyleForType(type)
        val iconResId = getIconForType(type)

        context?.let { ctx ->
            templateCard.setCardBackgroundColor(ContextCompat.getColor(ctx, getColorForType(type)))
            templateTitle.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0)
        }

        // Add custom fields based on the type
        when (type) {
            "유통" -> showDistributionTemplate(styleResId)
            "F&B" -> showFnBTemplate(styleResId)
            "행사 알림" -> showEventAlertTemplate(styleResId)
            "리뷰" -> showReviewTemplate(styleResId)
            "명소 추천" -> showAttractionTemplate(styleResId)
            "약속 장소" -> showMeetingPointTemplate(styleResId)
            "여행 메모" -> showTravelNoteTemplate(styleResId)
            "할인 요청" -> showDiscountRequestTemplate(styleResId)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDistributionTemplate(styleResId: Int) {
        addStyledField("타이틀", styleResId)
        addStyledField("부제목", styleResId, true)
        addStyledField("상품명", styleResId)
        addStyledField("판매 수량", styleResId)
        addStyledField("세부사항", styleResId, true)

        val discountTypeGroup = RadioGroup(ContextThemeWrapper(context, styleResId))
        val simpleDiscount = RadioButton(ContextThemeWrapper(context, styleResId)).apply {
            text = "단순 할인"
        }
        val bundleDiscount = RadioButton(ContextThemeWrapper(context, styleResId)).apply {
            text = "묶음 할인"
        }
        discountTypeGroup.addView(simpleDiscount)
        discountTypeGroup.addView(bundleDiscount)

        discountTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            templateContent.removeAllViews()
            addStyledField("상품명", styleResId)
            addStyledField("판매 수량", styleResId)
            addStyledField("세부사항", styleResId, true)

            when (checkedId) {
                simpleDiscount.id -> {
                    addStyledField("할인율 또는 할인가", styleResId)
                }
                bundleDiscount.id -> {
                    addStyledField("N+1", styleResId)
                }
            }
        }

        templateContent.addView(discountTypeGroup)
    }

    @SuppressLint("SetTextI18n")
    private fun showFnBTemplate(styleResId: Int) {
        addStyledField("업소명", styleResId)
        addStyledField("운영 시간", styleResId)
        addStyledField("세부 사항", styleResId, true)
        addStyledField("메뉴 이름", styleResId)
        addStyledField("메뉴 가격", styleResId)
        addStyledField("메뉴 설명", styleResId, true)

        val reservationMethodGroup = ChipGroup(ContextThemeWrapper(context, styleResId)).apply {
            addView(Chip(context).apply { text = "전화" })
            addView(Chip(context).apply { text = "웹사이트" })
            addView(Chip(context).apply { text = "앱" })
        }
        templateContent.addView(reservationMethodGroup)

        addStyledField("추가 혜택", styleResId, true)
        addStyledField("예약 인원 수에 따른 할인", styleResId, true)
        addStyledField("특별 이벤트", styleResId, true)
        addStyledField("단체 할인", styleResId, true)
    }

    @SuppressLint("SetTextI18n")
    private fun showEventAlertTemplate(styleResId: Int) {
        addStyledField("행사 제목", styleResId)
        addStyledField("행사 날짜 및 시간", styleResId)
        addStyledField("행사 장소", styleResId)
        addStyledField("행사 내용", styleResId, true)
        addStyledField("참가 방법", styleResId, true)

        val costTypeGroup = RadioGroup(ContextThemeWrapper(context, styleResId))
        val freeCost = RadioButton(ContextThemeWrapper(context, styleResId)).apply {
            text = "무료"
        }
        val paidCost = RadioButton(ContextThemeWrapper(context, styleResId)).apply {
            text = "유료"
        }
        costTypeGroup.addView(freeCost)
        costTypeGroup.addView(paidCost)

        costTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == paidCost.id) {
                addStyledField("참가비", styleResId)
            } else {
                // Remove the participant fee field if "무료" is selected
                templateContent.removeViewAt(templateContent.childCount - 1)
            }
        }

        templateContent.addView(costTypeGroup)
        addStyledField("등록 마감일", styleResId)
        addStyledField("주최 정보", styleResId, true)
        addStyledField("추가 정보", styleResId, true)
        addStyledField("등록 링크", styleResId)
    }

    private fun showReviewTemplate(styleResId: Int) {
        addStyledField("장소명", styleResId)
        addStyledField("업소 이름", styleResId)
        addStyledField("업소 위치", styleResId)
        addRatingBar(styleResId)
        addStyledField("장점", styleResId, true)
        addStyledField("단점", styleResId, true)
        addStyledField("가장 기억에 남는 특징이나 경험", styleResId, true)
        addStyledField("추천 대상", styleResId, true)
        addStyledField("추가 커멘트", styleResId, true)

        val revisitGroup = RadioGroup(ContextThemeWrapper(context, styleResId))
        val yesButton = RadioButton(ContextThemeWrapper(context, styleResId)).apply {
            text = "예"
        }
        val noButton = RadioButton(ContextThemeWrapper(context, styleResId)).apply {
            text = "아님"
        }
        revisitGroup.addView(yesButton)
        revisitGroup.addView(noButton)

        revisitGroup.setOnCheckedChangeListener { _, checkedId ->
            // Handle revisit selection if needed
        }

        templateContent.addView(revisitGroup)
    }

    private fun showAttractionTemplate(styleResId: Int) {
        addStyledField("명소 이름", styleResId)
        addStyledField("명소 위치", styleResId)
        addStyledField("한 줄 소개", styleResId)
        addStyledField("최적의 방문 시기 (계절, 월 또는 시간대)", styleResId)
        addStyledField("특별한 이유", styleResId, true)
        addStyledField("꼭 봐야 할 것들", styleResId, true)
        addStyledField("알아두면 좋은 팁", styleResId, true)
        addStyledField("이런 분들한테 추천", styleResId, true)
        addStyledField("인생샷 스팟", styleResId, true)
        addStyledField("한마디 표현", styleResId, true)
    }


    private fun showMeetingPointTemplate(styleResId: Int) {
        addStyledField("약속 장소", styleResId)
        addStyledField("장소 위치", styleResId)
        addStyledField("누구와", styleResId)
        addStyledField("언제", styleResId)
        addStyledField("목적", styleResId)
        addStyledField("준비물", styleResId, true)
    }


    private fun showTravelNoteTemplate(styleResId: Int) {
        addStyledField("장소 이름", styleResId)
        addStyledField("장소 위치", styleResId)
        addStyledField("날짜", styleResId)
        addStyledField("순간의 감정", styleResId, true)
        addStyledField("오감 (시각, 청각, 촉각, 후각, 미각)", styleResId, true)
        addStyledField("인상 깊은 장면&순간", styleResId, true)
        addStyledField("특별히 기억에 남는 사람 & 대화", styleResId, true)
        addStyledField("예상치 못한 경험", styleResId, true)
        addStyledField("이곳에서의 나의 감정 & 생각 & 변화", styleResId, true)
        addStyledField("떠오르는 생각들", styleResId, true)
        addStyledField("남기고 싶은 한마디", styleResId, true)
    }


    private fun showDiscountRequestTemplate(styleResId: Int) {
        addStyledField("상품명", styleResId)
        addStyledField("현재 가격", styleResId)
        addStyledField("요청 할인율", styleResId)
        addStyledField("요청 사유", styleResId, true)
    }

    private fun addStyledField(hint: String, styleResId: Int, multiLine: Boolean = false) {
        val inputLayout = TextInputLayout(ContextThemeWrapper(context, styleResId))
        val editText =  TextInputEditText(inputLayout.context)
        editText.hint = hint
        if (multiLine) {
            editText.minLines = 3
            editText.maxLines = 5
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        inputLayout.addView(editText)
        templateContent.addView(inputLayout)
    }

    private fun addRatingBar(styleResId: Int) {
        val ratingBar = RatingBar(ContextThemeWrapper(context, styleResId))
        ratingBar.numStars = 5
        ratingBar.stepSize = 0.5f
        templateContent.addView(ratingBar)
    }

    private fun getStyleForType(type: String): Int {
        return when (type) {
            "유통" -> R.style.TemplateStyle_Distribution
            "F&B" -> R.style.TemplateStyle_FnB
            "행사 알림" -> R.style.TemplateStyle_Event
            "리뷰" -> R.style.TemplateStyle_Review
            "명소 추천" -> R.style.TemplateStyle_Attraction
            "약속 장소" -> R.style.TemplateStyle_MeetingPoint
            "여행 메모" -> R.style.TemplateStyle_TravelNote
            "할인 요청" -> R.style.TemplateStyle_DiscountRequest
            else -> R.style.TemplateStyle_Default
        }
    }

    private fun getColorForType(type: String): Int {
        return when (type) {
            "유통" -> R.color.template_distribution
            "F&B" -> R.color.template_fnb
            "행사 알림" -> R.color.template_event
            "리뷰" -> R.color.template_review
            "명소 추천" -> R.color.template_attraction
            "약속 장소" -> R.color.template_meeting_point
            "여행 메모" -> R.color.template_travel_note
            "할인 요청" -> R.color.template_discount_request
            else -> R.color.template_default
        }
    }

    private fun getIconForType(type: String): Int {
        return when (type) {
            "유통" -> R.drawable.ic_distribution
            "F&B" -> R.drawable.ic_food
            "행사 알림" -> R.drawable.ic_event
            "리뷰" -> R.drawable.ic_review
            "명소 추천" -> R.drawable.ic_attraction
            "약속 장소" -> R.drawable.ic_meeting
            "여행 메모" -> R.drawable.ic_travel
            "할인 요청" -> R.drawable.ic_discount
            else -> R.drawable.ic_default
        }
    }
}
