package com.example.mappin_fe.Profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mappin_fe.Data.CouponResponse
import com.example.mappin_fe.R

class CouponAdapter(private val onCouponClick: (CouponResponse) -> Unit) :
    RecyclerView.Adapter<CouponAdapter.CouponViewHolder>() {

    private var coupon: CouponResponse? = null // 단일 CouponResponse 객체

    fun submitCoupon(newCoupon: CouponResponse) {
        coupon = newCoupon
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coupon, parent, false)
        return CouponViewHolder(view)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        coupon?.let { couponResponse -> // coupon이 null이 아닐 때만 바인딩
            holder.bind(couponResponse)
            holder.itemView.setOnClickListener { onCouponClick(couponResponse) } // couponResponse 전달
        }
    }

    override fun getItemCount() = if (coupon != null) 1 else 0 // 쿠폰이 있을 때만 1 반환

    class CouponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val couponCodeTextView: TextView = itemView.findViewById(R.id.couponCodeTextView)
        private val discountTextView: TextView = itemView.findViewById(R.id.discountTextView)

        fun bind(couponResponse: CouponResponse) {
            couponCodeTextView.text = couponResponse.coupon.code
            discountTextView.text = "${couponResponse.coupon.discount_amount}% 할인"
        }
    }
}
