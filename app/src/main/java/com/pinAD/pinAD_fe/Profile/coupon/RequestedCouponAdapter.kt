package com.pinAD.pinAD_fe.Profile.coupon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pinAD.pinAD_fe.Data.coupon.RequestedCouponResponse
import com.pinAD.pinAD_fe.R

class RequestedCouponAdapter(
    private val onRequestedCouponClick: (RequestedCouponResponse) -> Unit
) : RecyclerView.Adapter<RequestedCouponAdapter.RequestedCouponViewHolder>() {

    private var requestedCoupons: List<RequestedCouponResponse> = emptyList()

    fun submitRequestedCoupons(couponList: List<RequestedCouponResponse>) {
        this.requestedCoupons = couponList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestedCouponViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_requested_coupon, parent, false)
        return RequestedCouponViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestedCouponViewHolder, position: Int) {
        val couponResponse = requestedCoupons[position]
        holder.bind(couponResponse)
        holder.itemView.setOnClickListener { onRequestedCouponClick(couponResponse) }
    }

    override fun getItemCount() = requestedCoupons.size

    class RequestedCouponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val businessNameTextView: TextView = itemView.findViewById(R.id.businessNameTextView)
        private val couponCodeTextView: TextView = itemView.findViewById(R.id.couponCodeTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

        fun bind(requestedCoupon: RequestedCouponResponse) {
            businessNameTextView.text = requestedCoupon.business_name
            couponCodeTextView.text = requestedCoupon.coupon_code ?: "N/A" // Nullable 필드 처리
            statusTextView.text = requestedCoupon.status
        }
    }
}
