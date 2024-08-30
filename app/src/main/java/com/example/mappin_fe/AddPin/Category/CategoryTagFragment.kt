package com.example.mappin_fe.AddPin.Category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mappin_fe.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class CategoryTagFragment : Fragment() {
    private lateinit var chipGroupTags: ChipGroup
    private lateinit var etAddTag: EditText
    private lateinit var btnAddTag: Button
    private lateinit var tvInterestsLabel: TextView

    private val selectedColor = R.color.colorAccent
    private val defaultColor = R.color.colorChipBackground

    private lateinit var selectedSubCategory: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category_tag, container, false)

        chipGroupTags = view.findViewById(R.id.chip_group_tags)
        etAddTag = view.findViewById(R.id.et_add_tag)
        btnAddTag = view.findViewById(R.id.btn_add_tag)
        tvInterestsLabel = view.findViewById(R.id.tv_interests_label)

        // Retrieve selected subcategory from arguments
        selectedSubCategory = arguments?.getString("SELECTED_SUBCATEGORY") ?: ""

        btnAddTag.setOnClickListener {
            addNewTag()
        }

        // Initialize default tags based on selected subcategory
        initializeDefaultTags()

        return view
    }

    private fun initializeDefaultTags() {
        val defaultTags = getDefaultTagsForSubCategory(selectedSubCategory)
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
}
