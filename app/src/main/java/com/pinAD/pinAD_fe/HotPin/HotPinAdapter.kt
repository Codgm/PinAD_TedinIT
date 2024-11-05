package com.pinAD.pinAD_fe.HotPin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pinAD.pinAD_fe.Data.pin.FltPinData
import com.pinAD.pinAD_fe.R

class HotPinAdapter : ListAdapter<FltPinData, HotPinAdapter.PinViewHolder>(PinDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hot_pin_item_layout, parent, false)
        return PinViewHolder(view)
    }

    override fun onBindViewHolder(holder: PinViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewPin)
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)

        fun bind(pin: FltPinData) {
            titleTextView.text = pin.title
            descriptionTextView.text = pin.description

            // Load image using Glide
            Glide.with(itemView.context)
                .load(pin.media)
//                .placeholder(R.drawable.placeholder_image)
//                .error(R.drawable.error_image)
                .centerCrop()
                .into(imageView)

            // Set click listener
            itemView.setOnClickListener {
                // Handle click event (e.g., open detail view)
                // You can use an interface or callback to communicate back to the fragment/activity
            }
        }
    }

    class PinDiffCallback : DiffUtil.ItemCallback<FltPinData>() {
        override fun areItemsTheSame(oldItem: FltPinData, newItem: FltPinData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FltPinData, newItem: FltPinData): Boolean {
            return oldItem == newItem
        }
    }
}
