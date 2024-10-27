package com.example.mappin_fe.AddPin.Category

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.example.mappin_fe.AddPin.Camera.MediaFile
import com.example.mappin_fe.AddPin.PointPay.CouponPointFragment
import com.example.mappin_fe.AddPin.PointPay.PointSystemFragment
import com.example.mappin_fe.AddPin.Review.ReviewLocationFragment
import com.example.mappin_fe.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.File

class CategorySelectionFragment : Fragment() {

    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var templateCard: MaterialCardView
    private lateinit var templateTitle: TextView
    private lateinit var templateContent: LinearLayout
    private lateinit var nextButton: Button
    private lateinit var tagChipGroup: ChipGroup
    private lateinit var uploadButton: MaterialButton
    private lateinit var imagePreviewLayout: LinearLayout
    private  var mediaFiles =  mutableListOf<MediaFile>()
    private var is_ads: Boolean = false

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Get the real path from URI
                val realPath = getRealPathFromURI(uri)
                if (realPath != null) {
                    // Create a new MediaFile object
                    val mediaFile = MediaFile(
                        uri = realPath,
                        type = "image"
                    )

                    // Add to mediaFiles list
                    mediaFiles.add(mediaFile)

                    // Add preview
                    addImagePreview(uri, mediaFile)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category_selection, container, false)
        arguments?.let {
            val mediaFilesJson = it.getString("MEDIA_FILES")
            mediaFiles = Gson().fromJson(mediaFilesJson, object : TypeToken<List<MediaFile>>() {}.type)
        }

        categoryChipGroup = view.findViewById(R.id.categoryChipGroup)
        templateCard = view.findViewById(R.id.templateCard)
        templateTitle = view.findViewById(R.id.templateTitle)
        templateContent = view.findViewById(R.id.templateContent)
        nextButton = view.findViewById(R.id.btn_next)
        tagChipGroup = view.findViewById(R.id.tagChipGroup)
        uploadButton = view.findViewById(R.id.uploadButton)
        imagePreviewLayout = view.findViewById(R.id.imagePreviewLayout)

        setupCategories()
        setupUploadButton()
        setupNextButton()
        mediaFiles.forEach { mediaFile ->
            addImagePreview(Uri.parse(mediaFile.uri), mediaFile)
        }

        return view
    }

    private fun setupNextButton() {
        nextButton.setOnClickListener {
            if (areAllFieldsFilled()) {
                // Gather data and create bundle as before
                val (title, description, info) = gatherContentData()
                val selectedTags = getSelectedTags()

                val bundle = Bundle().apply {
                    putString("TITLE", title)
                    putString("DESCRIPTION", description)
                    putString("CATEGORY", getSelectedCategory())  // New function to get selected category
                    putString("INFO", info)
                    putBoolean("is_Ads", is_ads)
                    putStringArray("SELECTED_TAGS", selectedTags.toTypedArray())
                    putString("MEDIA_FILES", Gson().toJson(mediaFiles))
                }

                // Navigate based on the selected category
                when (getSelectedCategory()) {
                    "리뷰" -> {
                        val reviewLocationFragment = ReviewLocationFragment().apply {
                            arguments = bundle
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, reviewLocationFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    "쿠폰요청" -> {
                        val couponPointFragment = CouponPointFragment().apply {
                            arguments = bundle
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, couponPointFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    else -> {
                        val pointSystemFragment = PointSystemFragment().apply {
                            arguments = bundle
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, pointSystemFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            } else {
                Toast.makeText(context, "Please complete all required fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUploadButton() {
        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        context?.contentResolver?.query(uri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex)
            }
        }
        return uri.toString()
    }

    private fun addImagePreview(uri: Uri, mediaFile: MediaFile) {
        val imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 200, 1f) // 비율을 맞추기 위해 0으로 설정
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageURI(uri)
            setBackgroundResource(R.drawable.image_border) // 이미지 테두리 배경 추가
        }

        val deleteButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            layoutParams = LinearLayout.LayoutParams(50, 50).apply {
                marginStart = 8 // 버튼과 이미지 사이 여백 추가
            }
            background = null // 배경 제거
            setOnClickListener {
                // Remove from mediaFiles list
                mediaFiles.remove(mediaFile)
                Log.d("mediafiles", "$mediaFiles")

                // Remove preview
                val previewContainer = (parent as ViewGroup) // Get the parent view
                previewContainer.removeView(this) // Remove the delete button
                previewContainer.removeView(imageView) // Remove the image view
                imagePreviewLayout.removeView(previewContainer) // Remove the container
            }
        }

        // Create a container for the image and delete button
        val previewContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(8, 8, 8, 8) // 컨테이너에 패딩 추가
            setBackgroundResource(R.drawable.preview_background) // 배경 추가
            addView(imageView) // 이미지 미리보기 추가
            addView(deleteButton) // 삭제 버튼 추가
        }

        // Add the container to the image preview layout
        imagePreviewLayout.addView(previewContainer)
    }


    private fun getSelectedCategory(): String {
        val selectedChipId = categoryChipGroup.checkedChipId
        return if (selectedChipId != View.NO_ID) {
            (categoryChipGroup.findViewById<Chip>(selectedChipId))?.text.toString()
        } else {
            ""
        }
    }

    private fun gatherContentData(): Triple<String, String, String> {
        val titleBuilder = StringBuilder()
        val descriptionBuilder = StringBuilder()
        val infoBuilder = JSONObject()

        var fieldCount = 0
        for (i in 0 until templateContent.childCount) {
            val view = templateContent.getChildAt(i)
            if (view is TextInputLayout) {
                val editText = view.editText
                val text = editText?.text?.toString() ?: ""

                when (fieldCount) {
                    0 -> titleBuilder.append(text)
                    1 -> descriptionBuilder.append(text)
                    else -> infoBuilder.put("field${fieldCount - 1}", text)
                }
                fieldCount++
            } else if (view is RatingBar) {
                infoBuilder.put("rating", view.rating)
            }
        }

        return Triple(
            titleBuilder.toString(),
            descriptionBuilder.toString(),
            infoBuilder.toString()
        )
    }


    private fun areAllFieldsFilled(): Boolean {
        if (categoryChipGroup.checkedChipId == -1) return false

        var filledFields = 0
        for (i in 0 until templateContent.childCount) {
            val view = templateContent.getChildAt(i)
            if (view is TextInputLayout) {
                val editText = view.editText
                if (!editText?.text.isNullOrBlank()) {
                    filledFields++
                }
            }
        }

        return filledFields >= 2 // At least title and description should be filled
    }

    private fun setupCategories() {
        val categories = listOf("광고", "쿠폰요청", "리뷰")
        categories.forEach { category ->
            val chip = Chip(context)
            chip.text = category
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onCategorySelected(category)
                }
            }
            categoryChipGroup.addView(chip)
        }
    }

    private fun onCategorySelected(category: String) {
        templateContent.removeAllViews()
        tagChipGroup.removeAllViews()

        // 스타일, 색상, 아이콘 적용
        val styleResId = getStyleForType(category)
        val colorResId = getColorForType(category)
        val iconResId = getIconForType(category)

        context?.let { ctx ->
            templateCard.setCardBackgroundColor(ContextCompat.getColor(ctx, colorResId))
            templateTitle.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0)
        }

        is_ads = category == "광고"
        when (category) {
            "광고" -> {
                showAdTemplate()
                setupTags(getAdTags())
            }
            "쿠폰요청" -> {
                showDiscountRequestTemplate()
                setupTags(getDiscountRequestTags())
            }
            "리뷰" -> {
                showReviewTemplate()
                setupTags(getReviewTags())
            }
        }
    }

    private fun showAdTemplate() {
        templateTitle.text = "광고"
        addStyledField("타이틀", R.style.TemplateStyle_Distribution)
        addStyledField("세부사항", R.style.TemplateStyle_Distribution, true)
        addStyledField("상품명", R.style.TemplateStyle_Distribution)
        addStyledField("판매 수량", R.style.TemplateStyle_Distribution)
        addStyledField("할인율 또는 할인가", R.style.TemplateStyle_Distribution)
    }

    private fun showDiscountRequestTemplate() {
        templateTitle.text = "쿠폰요청"
        addStyledField("상품명", R.style.TemplateStyle_DiscountRequest)
        addStyledField("요청 할인율", R.style.TemplateStyle_DiscountRequest)
        addStyledField("요청 사유", R.style.TemplateStyle_DiscountRequest, true)
    }


    private fun showReviewTemplate() {
        templateTitle.text = "리뷰"
        val styleResId = getStyleForType("리뷰")
        addStyledField("제품명 / 서비스명", styleResId)
        addStyledField("제품/서비스 위치", styleResId)
        addRatingBar(styleResId)
        addStyledField("장점", styleResId, true)
        addStyledField("단점", styleResId, true)
    }

//    private fun showAttractionTemplate(styleResId: Int) {
//        addStyledField("명소 이름", styleResId)
//        addStyledField("한 줄 소개", styleResId)
//        addStyledField("명소 위치", styleResId)
//        addStyledField("최적의 방문 시기 (계절, 월 또는 시간대)", styleResId)
//        addStyledField("특별한 이유", styleResId, true)
//        addStyledField("꼭 봐야 할 것들", styleResId, true)
//        addStyledField("알아두면 좋은 팁", styleResId, true)
//        addStyledField("이런 분들한테 추천", styleResId, true)
//        addStyledField("인생샷 스팟", styleResId, true)
//        addStyledField("한마디 표현", styleResId, true)
//    }
//
//
//    private fun showMeetingPointTemplate(styleResId: Int) {
//        addStyledField("약속 장소", styleResId)
//        addStyledField("목적", styleResId)
//        addStyledField("장소 위치", styleResId)
//        addStyledField("누구와", styleResId)
//        addStyledField("언제", styleResId)
//        addStyledField("준비물", styleResId, true)
//    }
//
//
//    private fun showTravelNoteTemplate(styleResId: Int) {
//        addStyledField("장소 이름", styleResId)
//        addStyledField("장소 위치", styleResId)
//        addStyledField("날짜", styleResId)
//        addStyledField("순간의 감정", styleResId, true)
//        addStyledField("오감 (시각, 청각, 촉각, 후각, 미각)", styleResId, true)
//        addStyledField("인상 깊은 장면&순간", styleResId, true)
//        addStyledField("특별히 기억에 남는 사람 & 대화", styleResId, true)
//        addStyledField("예상치 못한 경험", styleResId, true)
//        addStyledField("이곳에서의 나의 감정 & 생각 & 변화", styleResId, true)
//        addStyledField("떠오르는 생각들", styleResId, true)
//        addStyledField("남기고 싶은 한마디", styleResId, true)
//    }
//
//
//    private fun showDiscountRequestTemplate(styleResId: Int) {
//        addStyledField("상품명", styleResId)
//        addStyledField("현재 가격", styleResId)
//        addStyledField("요청 할인율", styleResId)
//        addStyledField("요청 사유", styleResId, true)
//    }


    private fun setupTags(tags: List<String>) {
        tagChipGroup.removeAllViews()
        tagChipGroup.isSingleSelection = false
        tags.forEach { tag ->
            val chip = Chip(context).apply {
                text = tag
                isCheckable = true  // 다중 선택 가능하도록 설정
                setOnCheckedChangeListener { _, isChecked ->
                    val selectedTagsCount = getSelectedTags().size
                    if (isChecked && selectedTagsCount > 10) {
                        Toast.makeText(context, "최대 10개의 태그만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            tagChipGroup.addView(chip)
        }
    }


    private fun getAdTags(): List<String> {
        return listOf("뷰티", "전자기기", "가구", "식품", "생활용품", "베스트셀러", "도서", "스포츠/레저", "자동차용품", "의류")
    }

    private fun getDiscountRequestTags(): List<String> {
        return listOf("뷰티", "전자기기", "가구", "식품", "생활용품", "베스트셀러", "도서", "스포츠/레저", "자동차용품", "의류")
    }

    private fun getReviewTags(): List<String> {
        return listOf("뷰티", "전자기기", "가구", "식품", "생활용품", "베스트셀러", "도서", "스포츠/레저", "자동차용품", "의류")
    }

    private fun getSelectedTags(): List<String> {
        return (0 until tagChipGroup.childCount)
            .map { tagChipGroup.getChildAt(it) as Chip }
            .filter { it.isChecked }
            .map { it.text.toString() }
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
            "광고" -> R.style.TemplateStyle_Distribution
            "F&B" -> R.style.TemplateStyle_FnB
            "행사 알림" -> R.style.TemplateStyle_Event
            "리뷰" -> R.style.TemplateStyle_Review
            "명소 추천" -> R.style.TemplateStyle_Attraction
            "약속 장소" -> R.style.TemplateStyle_MeetingPoint
            "여행 메모" -> R.style.TemplateStyle_TravelNote
            "쿠폰 요청" -> R.style.TemplateStyle_DiscountRequest
            else -> R.style.TemplateStyle_Default
        }
    }

    private fun getColorForType(type: String): Int {
        return when (type) {
            "광고" -> R.color.template_distribution
            "F&B" -> R.color.template_fnb
            "행사 알림" -> R.color.template_event
            "리뷰" -> R.color.template_review
            "명소 추천" -> R.color.template_attraction
            "약속 장소" -> R.color.template_meeting_point
            "여행 메모" -> R.color.template_travel_note
            "쿠폰 요청" -> R.color.template_discount_request
            else -> R.color.template_default
        }
    }

    private fun getIconForType(type: String): Int {
        return when (type) {
            "광고" -> R.drawable.ic_distribution
            "F&B" -> R.drawable.ic_food
            "행사 알림" -> R.drawable.ic_event
            "리뷰" -> R.drawable.ic_review
            "명소 추천" -> R.drawable.ic_attraction
            "약속 장소" -> R.drawable.ic_meeting
            "여행 메모" -> R.drawable.ic_travel
            "쿠폰 요청" -> R.drawable.ic_discount
            else -> R.drawable.ic_default
        }
    }
}
