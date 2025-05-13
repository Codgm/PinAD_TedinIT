package com.pinAD.pinAD_fe.Profile.coupon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pinAD.pinAD_fe.Data.coupon.CouponResponse
import com.pinAD.pinAD_fe.R

class CouponAdapter(private val onCouponClick: (CouponResponse) -> Unit) :
    RecyclerView.Adapter<CouponAdapter.CouponViewHolder>() {

    private var coupons: List<CouponResponse> = emptyList()// 단일 CouponResponse 객체

    fun submitCoupons(couponList: List<CouponResponse>) {
        this.coupons = couponList
        notifyDataSetChanged() // 데이터 변경 알림
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coupon, parent, false)
        return CouponViewHolder(view)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        val couponResponse = coupons[position] // coupons 리스트에서 현재 위치의 CouponResponse 가져오기
        holder.bind(couponResponse) // 쿠폰 바인딩
        holder.itemView.setOnClickListener { onCouponClick(couponResponse) }
    }

    override fun getItemCount() = coupons.size

    class CouponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val couponCodeTextView: TextView = itemView.findViewById(R.id.couponCodeTextView)
        private val discountTextView: TextView = itemView.findViewById(R.id.discountTextView)

        fun bind(couponResponse: CouponResponse) {
            couponCodeTextView.text = couponResponse.coupon.code
            discountTextView.text = "${couponResponse.coupon.discount_amount}% ${itemView.context.getString(R.string.coupon_discount_format)}"
        }
    }
}
