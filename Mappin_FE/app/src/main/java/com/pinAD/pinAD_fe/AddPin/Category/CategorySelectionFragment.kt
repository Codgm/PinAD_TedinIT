package com.pinAD.pinAD_fe.AddPin.Category

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
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
import com.pinAD.pinAD_fe.AddPin.Camera.MediaFile
import com.pinAD.pinAD_fe.AddPin.PointPay.CouponPointFragment
import com.pinAD.pinAD_fe.AddPin.PointPay.PointSystemFragment
import com.pinAD.pinAD_fe.AddPin.Review.ReviewLocationFragment
import com.pinAD.pinAD_fe.R
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
                val selectedCategory = getSelectedCategory()

                val pin_type = when (selectedCategory) {
                    getString(R.string.category_review) -> 2
                    getString(R.string.category_coupon) -> 1
                    else -> 0
                }
                // Gather data and create bundle as before
                val (title, description, info) = when (pin_type) {
                    2 -> gatherReviewData()
                    else -> gatherContentData()
                }
                val selectedTags = getSelectedTags()

                val bundle = Bundle().apply {
                    putString("TITLE", title)
                    putString("DESCRIPTION", description)
                    putString("CATEGORY", getSelectedCategory())  // New function to get selected category
                    putString("INFO", info)
                    putBoolean("is_Ads", is_ads)
                    putStringArray("SELECTED_TAGS", selectedTags.toTypedArray())
                    putString("MEDIA_FILES", Gson().toJson(mediaFiles))
                    putInt("PIN_TYPE", pin_type)
                }

                // Navigate based on the selected category
                when (pin_type) {
                    2 -> {
                        val reviewLocationFragment = ReviewLocationFragment().apply {
                            arguments = bundle
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, reviewLocationFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    1 -> {
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
        val previewContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            background = context.getDrawable(R.drawable.preview_background) // 그림자 효과
        }

        // 미디어 파일이 이미지인지 비디오인지 확인
        if (mediaFile.type == "image") {
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 200, 1f).apply {
                    setMargins(0, 16, 16, 0) // 이미지와 삭제 버튼 간격
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
                setBackgroundResource(R.drawable.image_border) // 테두리
            }
            previewContainer.addView(imageView)
        } else if (mediaFile.type == "video") {
            val videoThumbnail = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 200, 1f).apply {
                    setMargins(0, 16, 16, 0) // 비디오와 삭제 버튼 간격
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageBitmap(getVideoThumbnail(uri)) // 비디오의 썸네일을 설정
                setBackgroundResource(R.drawable.image_border) // 테두리
            }
            previewContainer.addView(videoThumbnail)

            videoThumbnail.setOnClickListener {
                val videoView = VideoView(context).apply {
                    setVideoURI(uri)
                    start()
                }
                previewContainer.addView(videoView)
            }
        }

        val deleteButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                setMargins(8, 0, 0, 0) // 버튼과 미디어 간격
            }
            background = null
            setOnClickListener {
                mediaFiles.remove(mediaFile)
                Log.d("mediafiles", "$mediaFiles")

                val previewParent = (parent as ViewGroup)
                previewParent.removeView(this)
                previewParent.removeViewAt(0) // 이미지 또는 비디오 뷰 삭제
                imagePreviewLayout.removeView(previewParent)
            }
        }
        previewContainer.addView(deleteButton)

        imagePreviewLayout.addView(previewContainer)
    }

    // 비디오 썸네일을 생성하는 함수
    private fun getVideoThumbnail(uri: Uri): Bitmap? {
        return ThumbnailUtils.createVideoThumbnail(File(uri.path).toString(), MediaStore.Images.Thumbnails.MINI_KIND)
    }



    private fun getSelectedCategory(): String {
        val selectedChipId = categoryChipGroup.checkedChipId
        return if (selectedChipId != View.NO_ID) {
            (categoryChipGroup.findViewById<Chip>(selectedChipId))?.text.toString()
        } else {
            ""
        }
    }

    private fun gatherReviewData(): Triple<String, String, String> {
        val titleBuilder = StringBuilder()
        val descriptionBuilder = StringBuilder()
        val infoBuilder = JSONObject()

        var fieldCount = 0
        for (i in 0 until templateContent.childCount) {
            val view = templateContent.getChildAt(i)
            when (view) {
                is TextInputLayout -> {
                    val editText = view.editText
                    val text = editText?.text?.toString() ?: ""

                    when (fieldCount) {
                        0 -> titleBuilder.append(text)
                        1 -> descriptionBuilder.append(text)
                        2 -> {
                            infoBuilder.put("advantages", text)
                        }

                        3 -> {
                            infoBuilder.put("disadvantages", text)
                        }
                        4 -> {infoBuilder.put("max_issued_count",  text.toIntOrNull() ?: 0)}
                    }
                    fieldCount++
                }

                is RatingBar -> {
                    infoBuilder.put("rating", view.rating)
                }
            }
        }
        return Triple(
            titleBuilder.toString(),
            descriptionBuilder.toString(),
            infoBuilder.toString()
        )
    }

    private fun gatherContentData(): Triple<String, String, String> {
        val titleBuilder = StringBuilder()
        val descriptionBuilder = StringBuilder()
        val infoBuilder = JSONObject()

        var fieldCount = 0
        for (i in 0 until templateContent.childCount) {
            val view = templateContent.getChildAt(i)
            when (view) {
                is TextInputLayout -> {
                    val editText = view.editText
                    val text = editText?.text?.toString() ?: ""

                    when (fieldCount) {
                        0 -> titleBuilder.append(text)
                        1 -> descriptionBuilder.append(text)
                        2 -> infoBuilder.put("product_name", text)
                        3 -> infoBuilder.put("max_issued_count", text.toIntOrNull() ?: 0)
                        4 -> infoBuilder.put("discount_amount", text.toIntOrNull() ?: 0)
                    }
                    fieldCount++
                }
                is RatingBar ->{
                    infoBuilder.put("rating", view.rating)
                }
                is RadioGroup -> {
                    val selectedId = view.checkedRadioButtonId
                    if (selectedId != -1) {
                        val selectedBtn = view.findViewById<RadioButton>(selectedId)
                        infoBuilder.put("discount_type",
                            if (selectedBtn.text == getString(R.string.discount_type_percent)) "PERCENTAGE"
                            else "FIXED"
                        )
                    }
                }
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
        val categories = listOf(
            getString(R.string.category_ad),
            getString(R.string.category_coupon),
            getString(R.string.category_review)
        )
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

        is_ads = category == getString(R.string.category_ad)
        when (category) {
            getString(R.string.category_ad) -> {
                showAdTemplate()
                setupTags(getAdTags())
            }
            getString(R.string.category_coupon) -> {
                showDiscountRequestTemplate()
                setupTags(getDiscountRequestTags())
            }
            getString(R.string.category_review) -> {
                showReviewTemplate()
                setupTags(getReviewTags())
            }
        }
    }

    private fun showAdTemplate() {
        templateTitle.text = getString(R.string.category_ad)

        // 할인 타입 라디오 그룹 추가
        val radioGroup = RadioGroup(context).apply {
            orientation = RadioGroup.HORIZONTAL
            val percentBtn = RadioButton(context).apply {
                text = getString(R.string.discount_type_percent)
                id = View.generateViewId()
            }
            val fixedBtn = RadioButton(context).apply {
                text = getString(R.string.discount_type_fixed)
                id = View.generateViewId()
            }
            addView(percentBtn)
            addView(fixedBtn)
        }

        addStyledField(getString(R.string.field_title), R.style.TemplateStyle_Distribution)
        addStyledField(getString(R.string.field_details), R.style.TemplateStyle_Distribution, true)
        addStyledField(getString(R.string.field_product_name), R.style.TemplateStyle_Distribution)
        addStyledField(getString(R.string.field_quantity), R.style.TemplateStyle_Distribution)
        templateContent.addView(radioGroup)
        addStyledField(getString(R.string.field_discount_amount), R.style.TemplateStyle_Distribution)
    }


    private fun showDiscountRequestTemplate() {
        templateTitle.text = getString(R.string.category_coupon)
        addStyledField(getString(R.string.field_title), R.style.TemplateStyle_DiscountRequest)
        addStyledField(getString(R.string.field_discount_amount), R.style.TemplateStyle_DiscountRequest)
        // Radio buttons for discount type
        val radioGroup = RadioGroup(context).apply {
            orientation = RadioGroup.HORIZONTAL
            val percentBtn = RadioButton(context).apply {
                text = getString(R.string.discount_type_percent)
                id = View.generateViewId()
            }
            val fixedBtn = RadioButton(context).apply {
                text = getString(R.string.discount_type_fixed)
                id = View.generateViewId()
            }
            addView(percentBtn)
            addView(fixedBtn)
        }
        templateContent.addView(radioGroup)
        addStyledField(getString(R.string.field_request_reason), R.style.TemplateStyle_DiscountRequest, true)
        addStyledField(getString(R.string.field_max_issued_count), R.style.TemplateStyle_DiscountRequest)
    }


    private fun showReviewTemplate() {
        templateTitle.text = getString(R.string.category_review)
        val styleResId = getStyleForType(getString(R.string.category_review))
        addStyledField(getString(R.string.review_product_name), styleResId)
        addStyledField(getString(R.string.review_product_location), styleResId)
        addRatingBar(styleResId)
        addStyledField(getString(R.string.review_advantages), styleResId, true)
        addStyledField(getString(R.string.review_disadvantages), styleResId, true)
    }

    private fun setupTags(tags: List<String>) {
        val customTagContainer = view?.findViewById<LinearLayout>(R.id.customTagContainer)
        val customTagInput = view?.findViewById<EditText>(R.id.customTagInput)
        val confirmButton = view?.findViewById<Button>(R.id.confirmButton)
        tagChipGroup.removeAllViews()
        tagChipGroup.isSingleSelection = false
        tags.forEach { tag ->
            val chip = Chip(context).apply {
                text = tag
                isCheckable = true  // 다중 선택 가능하도록 설정
                setOnCheckedChangeListener { _, isChecked ->
                    if (text == getString(R.string.tag_other) && isChecked) {
                        // "기타" 태그 클릭 시 사용자 정의 태그 입력창 표시
                        customTagContainer?.visibility = View.VISIBLE
                    }
                    val selectedTagsCount = getSelectedTags().size
                    if (isChecked && selectedTagsCount > 10) {
                        Toast.makeText(context, "최대 10개의 태그만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            tagChipGroup.addView(chip)
        }
        confirmButton?.setOnClickListener {
            val newTag = customTagInput?.text.toString()
            if (newTag.isNotBlank()) {
                addCustomTag(newTag)
                customTagInput?.text?.clear()
                customTagContainer?.visibility = View.GONE  // 태그 입력 후 숨기기
            }
        }
    }

    private fun addCustomTag(tag: String) {
        val customChip = Chip(context).apply {
            text = tag
            isCheckable = true
        }
        tagChipGroup.addView(customChip)
    }


    private fun getAdTags(): List<String> {
        return listOf(
            getString(R.string.tag_food_drink),
            getString(R.string.tag_clothing_shoes),
            getString(R.string.tag_beauty_health),
            getString(R.string.tag_sports_leisure),
            getString(R.string.tag_convenience_mart),
            getString(R.string.tag_culture_entertainment),
            getString(R.string.tag_education_books),
            getString(R.string.tag_automotive),
            getString(R.string.tag_life_service),
            getString(R.string.tag_event),
            getString(R.string.tag_other)
        )
    }

    private fun getDiscountRequestTags(): List<String> {
        return listOf(
            getString(R.string.tag_food_drink),
            getString(R.string.tag_clothing_shoes),
            getString(R.string.tag_beauty_health),
            getString(R.string.tag_sports_leisure),
            getString(R.string.tag_convenience_mart),
            getString(R.string.tag_culture_entertainment),
            getString(R.string.tag_education_books),
            getString(R.string.tag_automotive),
            getString(R.string.tag_life_service),
            getString(R.string.tag_event),
            getString(R.string.tag_other))
    }

    private fun getReviewTags(): List<String> {
        return listOf(
            getString(R.string.tag_food_drink),
            getString(R.string.tag_clothing_shoes),
            getString(R.string.tag_beauty_health),
            getString(R.string.tag_sports_leisure),
            getString(R.string.tag_convenience_mart),
            getString(R.string.tag_culture_entertainment),
            getString(R.string.tag_education_books),
            getString(R.string.tag_automotive),
            getString(R.string.tag_life_service),
            getString(R.string.tag_event),
            getString(R.string.tag_other))
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
            getString(R.string.category_ad) -> R.style.TemplateStyle_Distribution
            "F&B" -> R.style.TemplateStyle_FnB
            "행사 알림" -> R.style.TemplateStyle_Event
            getString(R.string.category_review) -> R.style.TemplateStyle_Review
            "명소 추천" -> R.style.TemplateStyle_Attraction
            "약속 장소" -> R.style.TemplateStyle_MeetingPoint
            "여행 메모" -> R.style.TemplateStyle_TravelNote
            getString(R.string.category_coupon) -> R.style.TemplateStyle_DiscountRequest
            else -> R.style.TemplateStyle_Default
        }
    }

    private fun getColorForType(type: String): Int {
        return when (type) {
            getString(R.string.category_ad) -> R.color.template_distribution
            "F&B" -> R.color.template_fnb
            "행사 알림" -> R.color.template_event
            getString(R.string.category_review) -> R.color.template_review
            "명소 추천" -> R.color.template_attraction
            "약속 장소" -> R.color.template_meeting_point
            "여행 메모" -> R.color.template_travel_note
            getString(R.string.category_coupon) -> R.color.template_discount_request
            else -> R.color.template_default
        }
    }

    private fun getIconForType(type: String): Int {
        return when (type) {
            getString(R.string.category_ad) -> R.drawable.ic_ad_creation
            "F&B" -> R.drawable.ic_food
            "행사 알림" -> R.drawable.ic_event
            getString(R.string.category_review) -> R.drawable.ic_review
            "명소 추천" -> R.drawable.ic_attraction
            "약속 장소" -> R.drawable.ic_meeting
            "여행 메모" -> R.drawable.ic_travel
            getString(R.string.category_coupon) -> R.drawable.ic_discount
            else -> R.drawable.ic_default
        }
    }
}
