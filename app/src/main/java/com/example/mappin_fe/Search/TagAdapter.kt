package com.example.mappin_fe.Search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mappin_fe.Data.Tag
import com.example.mappin_fe.R

class TagAdapter(
    private val tags: List<Tag>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(tag: Tag)
    }

    inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tagName: TextView = itemView.findViewById(R.id.tag_name)
        val tagCount: TextView = itemView.findViewById(R.id.tag_count)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(tags[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]
        holder.tagName.text = "#${tag.name}"
        holder.tagCount.text = "작성된 핀 ${tag.post_count}"
    }

    override fun getItemCount(): Int = tags.size
}