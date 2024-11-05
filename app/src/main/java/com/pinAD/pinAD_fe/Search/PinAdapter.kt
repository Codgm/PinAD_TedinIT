package com.pinAD.pinAD_fe.Search

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pinAD.pinAD_fe.Data.pin.FltPinData
import com.pinAD.pinAD_fe.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class PinAdapter(
    private val pinList: List<FltPinData>,
    private val onItemClick: (FltPinData) -> Unit,
    private val searchQuery: String,
    private val isTagSearch: Boolean = false
) : RecyclerView.Adapter<PinAdapter.PinViewHolder>() {

    class PinViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_title)
        val description: TextView = view.findViewById(R.id.tvDescription)
        val tagGroup: ChipGroup = view.findViewById(R.id.chipGroupTags)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinViewHolder {
        val layout = R.layout.pin_item_layout
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return PinViewHolder(view)
    }

    override fun onBindViewHolder(holder: PinViewHolder, position: Int) {
        val pin = pinList[position]

        // Highlight search query in title
        val titleSpannable = SpannableString(pin.title ?: "")
        if (!searchQuery.isBlank() && pin.title?.contains(searchQuery, ignoreCase = true) == true) {
            val startIndex = pin.title.toLowerCase().indexOf(searchQuery.toLowerCase())
            val endIndex = startIndex + searchQuery.length
            titleSpannable.setSpan(
                ForegroundColorSpan(Color.BLUE), // 텍스트 색상을 노란색으로 변경
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        holder.title.text = titleSpannable

        holder.description.text = pin.description ?: ""

        // Set up tags
        holder.tagGroup.removeAllViews()
        pin.tags.forEach { tag ->
            addChip(holder, tag.name) // FltPinData의 tags가 String 리스트로 가정
        }

        Log.d("PinAdapter", "Binding pin: ${pin.title}")
        holder.itemView.setOnClickListener {
            onItemClick(pin)
        }
    }

    private fun addChip(holder: PinViewHolder, tagName: String) {
        val chip = Chip(holder.itemView.context)
        chip.text = tagName

        when {
            isTagSearch && tagName.contains(searchQuery, ignoreCase = true) -> {
                Log.d("isTagSearch", "$isTagSearch")
                // 태그 검색 결과에서 일치하는 태그를 강조
                chip.setChipBackgroundColorResource(android.R.color.white)
                chip.setTextColor(Color.BLUE)
                chip.chipStrokeWidth = 1f
                chip.setChipStrokeColorResource(R.color.colorPrimary)
            }
            !isTagSearch && searchQuery.isNotBlank() && tagName.contains(searchQuery, ignoreCase = true) -> {
                // 일반 검색에서 검색어를 포함하는 태그를 강조
                Log.d("isTagSearch", "false")
                chip.setChipBackgroundColorResource(R.color.white)
                chip.setTextColor(Color.BLACK)
            }
            else -> {
                // 기본 스타일
                Log.d("default", "default")
                chip.setChipBackgroundColorResource(R.color.colorChipBackground)
                chip.setTextColor(Color.BLACK)
            }
        }

        // Chip의 패딩 설정
        val paddingDp = 8
        val density = holder.itemView.context.resources.displayMetrics.density
        val paddingPx = (paddingDp * density).toInt()
        chip.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)

        holder.tagGroup.addView(chip)
    }

    override fun getItemCount(): Int {
        Log.d("PinAdapter", "Total items: ${pinList.size}")
        return pinList.size
    }
}