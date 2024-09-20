package com.example.mappin_fe.AddPin.Category

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mappin_fe.AddPin.Camera.MediaFile
import com.example.mappin_fe.AddPin.PointPay.PointSystemFragment
import com.example.mappin_fe.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoryTagFragment : Fragment() {
    private lateinit var chipGroupTags: ChipGroup
    private lateinit var etAddTag: EditText
    private lateinit var btnAddTag: Button
    private lateinit var btnNext: Button
    private lateinit var tvInterestsLabel: TextView

    private val selectedColor = R.color.colorAccent
    private val defaultColor = R.color.colorChipBackground

    private lateinit var receivedMainCategory: String
    private lateinit var receivedSubCategory: String
    private lateinit var info: String
    private lateinit var title: String
    private lateinit var description: String
    private var is_ads: Boolean = false
    private lateinit var mediaFiles: List<MediaFile>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category_tag, container, false)

        chipGroupTags = view.findViewById(R.id.chip_group_tags)
        etAddTag = view.findViewById(R.id.et_add_tag)
        btnAddTag = view.findViewById(R.id.btn_add_tag)
        btnNext = view.findViewById(R.id.btn_next)
        tvInterestsLabel = view.findViewById(R.id.tv_interests_label)

        // Retrieve selected subcategory from arguments
        receivedMainCategory = arguments?.getString("SELECTED_MAIN_CATEGORY") ?: ""
        receivedSubCategory = arguments?.getString("SELECTED_SUBCATEGORY") ?: ""
        info = arguments?.getString("INFO") ?: ""
        title = arguments?.getString("TITLE") ?: ""
        description = arguments?.getString("DESCRIPTION") ?: ""
        is_ads = arguments?.getBoolean("is_Ads", false) ?: false
        // 미디어 파일 정보 받기
        arguments?.let {
            val mediaFilesJson = it.getString("MEDIA_FILES")
            mediaFiles = Gson().fromJson(mediaFilesJson, object : TypeToken<List<MediaFile>>() {}.type)
        }

        Log.d("CategoryTagFragment", "Number of media files: ${mediaFiles.size}")
        Log.d("CategoryTagFragment", "Selected Main Category: $receivedMainCategory")
        Log.d("CategoryTagFragment", "Selected Subcategory: $receivedSubCategory")
        Log.d("CategoryTagFragment", "Info: $info")
        Log.d("CategoryTagFragment", "Title: $title")
        Log.d("CategoryTagFragment", "Description: $description")
        Log.d("CategoryTagFragment", "is_Ads: $is_ads")

        btnAddTag.setOnClickListener {
            addNewTag()
        }

        btnNext.setOnClickListener {
            navigateToPointSystemFragment()
        }

        // Initialize default tags based on selected subcategory
        initializeDefaultTags()

        return view
    }

    private fun navigateToPointSystemFragment() {
        val pointSystemFragment = PointSystemFragment()
        val bundle = Bundle().apply {
            putString("SELECTED_MAIN_CATEGORY", receivedMainCategory)
            putString("SELECTED_SUBCATEGORY", receivedSubCategory)
            putString("INFO", info)
            putString("TITLE", title)
            putString("DESCRIPTION", description)
            putBoolean("is_Ads", is_ads)
            putStringArray("SELECTED_TAGS", getSelectedTags().toTypedArray())
            putString("MEDIA_FILES", Gson().toJson(mediaFiles))
        }
        pointSystemFragment.arguments = bundle

        // FragmentManager를 사용하여 Fragment 전환
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, pointSystemFragment) // FragmentContainer ID를 정확하게 설정해야 합니다.
            .addToBackStack(null) // 뒤로 가기 버튼을 누르면 이전 Fragment로 돌아가도록 설정
            .commit()
    }

    private fun initializeDefaultTags() {
        val defaultTags = getDefaultTagsForSubCategory(receivedSubCategory)
        for (tag in defaultTags) {
            val chip = Chip(requireContext()).apply {
                text = tag
                isCloseIconVisible = false
                setChipBackgroundColorResource(defaultColor)
                isClickable = true
                isCheckable = false
                setOnClickListener {
                    toggleChipSelection(this)
                }
            }
            chipGroupTags.addView(chip)
        }
    }

    private fun getDefaultTagsForSubCategory(subCategory: String): List<String> {
        return when (subCategory) {
            "유통" -> listOf("Wholesale", "Distribution", "Supply Chain", "Logistics", "Inventory Management")
            "F&B" -> listOf("Restaurant", "Cafe", "Bar", "Cuisine", "Food Delivery")
            "행사 알림" -> listOf("Event", "Seminar", "Workshop", "Conference", "Webinar")
            "리뷰" -> listOf("Restaurant Review", "Hotel Review", "Product Review", "Service Feedback", "Travel Review")
            "명소 추천" -> listOf("Popular Places", "Hidden Gems", "Must Visit", "Landmarks", "Scenic Views")
            "약속 장소" -> listOf("Meeting Spot", "Event Location", "Coffee Shop", "Office", "Outdoor Area")
            "여행 메모" -> listOf("Travel Diary", "Trip Highlights", "Itinerary", "Packing List", "Travel Tips")
            "할인 요청" -> listOf("Discount Request", "Promo Code", "Special Offer", "Coupon", "Group Discount")
            else -> listOf("Default Tag 1", "Default Tag 2")
        }
    }

    private fun addNewTag() {
        val newTag = etAddTag.text.toString().trim()
        if (newTag.isNotEmpty()) {
            val newChip = Chip(requireContext()).apply {
                text = newTag
                isCloseIconVisible = true
                setChipBackgroundColorResource(selectedColor)
                isClickable = true
                isCheckable = false
                tag = true
                setOnCloseIconClickListener {
                    chipGroupTags.removeView(this)
                }
                setOnClickListener {
                    toggleChipSelection(this)
                }
            }
            chipGroupTags.addView(newChip)
            etAddTag.text.clear()
        } else {
            Toast.makeText(requireContext(), "태그를 입력해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleChipSelection(chip: Chip) {
        val isChecked = chip.tag as? Boolean ?: false
        if (isChecked) {
            chip.setChipBackgroundColorResource(defaultColor)
            chip.tag = false
        } else {
            chip.setChipBackgroundColorResource(selectedColor)
            chip.tag = true
        }
    }

    private fun getSelectedTags(): List<String> {
        val selectedTags = mutableListOf<String>()
        for (i in 0 until chipGroupTags.childCount) {
            val view = chipGroupTags.getChildAt(i)
            if (view is Chip && view.tag as? Boolean == true) {
                selectedTags.add(view.text.toString())
            }
        }
        return selectedTags
    }
}
