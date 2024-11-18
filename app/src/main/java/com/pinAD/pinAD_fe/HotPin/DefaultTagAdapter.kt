package com.pinAD.pinAD_fe.HotPin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.pinAD.pinAD_fe.R

class DefaultTagAdapter(private val onTagSelected: (String) -> Unit) :
    ListAdapter<String, DefaultTagAdapter.TagViewHolder>(TagDiffCallback()) {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tag_text_view, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = getItem(position)
        holder.bind(tag, position == selectedPosition)
        holder.itemView.setOnClickListener {
            val previousSelected = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
            onTagSelected(tag)
        }
    }

    fun getSelectedTag(): String? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            getItem(selectedPosition)
        } else {
            null
        }
    }

    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chipTag: Chip = itemView.findViewById(R.id.recyclerViewTags)

        fun bind(tag: String, isSelected: Boolean) {
            chipTag.text = tag
            chipTag.isChecked = isSelected

            if (isSelected) {
                chipTag.setChipBackgroundColorResource(R.color.colorSecondary)  // 선택된 색상
                chipTag.setTextColor(itemView.context.getColor(R.color.white))
            } else {
                chipTag.setChipBackgroundColorResource(R.color.surfaceLight)  // 기본 색상
                chipTag.setTextColor(itemView.context.getColor(R.color.black))
            }
        }
    }

    private class TagDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}