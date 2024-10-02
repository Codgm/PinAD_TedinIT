package com.example.mappin_fe.Search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mappin_fe.Data.PinDataResponse
import com.example.mappin_fe.R

class PinAdapter(private val pinList: List<PinDataResponse>, private val onItemClick: (PinDataResponse) -> Unit) :
    RecyclerView.Adapter<PinAdapter.PinViewHolder>() {

    class PinViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_title)
        val description: TextView = view.findViewById(R.id.tvDescription)
        // 추가적으로 필요한 뷰들을 여기에 선언
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pin_item_layout, parent, false)
        return PinViewHolder(view)
    }

    override fun onBindViewHolder(holder: PinViewHolder, position: Int) {
        val pin = pinList[position]
        holder.title.text = pin.title
        holder.description.text = pin.description
        Log.d("PinAdapter", "Binding pin: ${pin.title}")
        holder.itemView.setOnClickListener {
            onItemClick(pin) // 클릭 시 핀 정보 전달
        }
    }

    override fun getItemCount(): Int {
        Log.d("PinAdapter", "Total items: ${pinList.size}")
        return pinList.size
    }
}
