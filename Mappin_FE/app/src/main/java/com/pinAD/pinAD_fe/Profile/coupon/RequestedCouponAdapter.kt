// RequestedCouponAdapter.kt
package com.pinAD.pinAD_fe.Profile.coupon

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.pinAD.pinAD_fe.Data.coupon.CouponStatusResponse
import com.pinAD.pinAD_fe.Data.coupon.RequestedCouponResponse
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RequestedCouponAdapter(
    private val coroutineScope: CoroutineScope
) : RecyclerView.Adapter<RequestedCouponAdapter.RequestedCouponViewHolder>() {

    private var requestedCoupons: List<RequestedCouponResponse> = emptyList()
    private var expandedPosition: Int = -1
    private var statusMap: MutableMap<String, CouponStatusResponse> = mutableMapOf()

    @SuppressLint("NotifyDataSetChanged")
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
        val isExpanded = position == expandedPosition

        holder.bind(couponResponse, statusMap[couponResponse.coupon_code], isExpanded)

        holder.itemView.setOnClickListener {
            val wasExpanded = expandedPosition == position
            expandedPosition = if (wasExpanded) -1 else position

            if (!wasExpanded && !statusMap.containsKey(couponResponse.coupon_code)) {
                holder.showLoading(true)
                fetchCouponStatus(holder.itemView.context, couponResponse.coupon_code, position)
            }

            notifyItemChanged(expandedPosition)
            if (wasExpanded) {
                notifyItemChanged(position)
            }
        }
    }

    private fun fetchCouponStatus(context: Context, couponId: String, position: Int) {
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.api.getCouponStatus(couponId)
                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful -> {
                            // 성공적으로 데이터를 받아온 경우
                            response.body()?.let { status ->
                                statusMap[couponId] = status
                                if (position == expandedPosition) {
                                    notifyItemChanged(position)
                                }
                            }
                        }
                        response.code() == 401 -> {
                            // 인증되지 않은 사용자
                            Toast.makeText(
                                context,
                                context.getString(R.string.login_required),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        response.code() == 404 -> {
                            // 존재하지 않는 쿠폰 ID
                            Toast.makeText(
                                context,
                                context.getString(R.string.coupon_not_found),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            // 기타 오류 처리
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_occurred),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // 네트워크 오류나 기타 예외 상황 처리
                    Toast.makeText(
                        context,
                        context.getString(R.string.network_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (position == expandedPosition) {
                        notifyItemChanged(position)
                    }
                }
            }
        }
    }



    override fun getItemCount() = requestedCoupons.size

    class RequestedCouponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val businessNameTextView: TextView = itemView.findViewById(R.id.businessNameTextView)
        private val couponCodeTextView: TextView = itemView.findViewById(R.id.couponCodeTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val statisticsContainer: View = itemView.findViewById(R.id.statisticsContainer)
        private val statisticsContent: View = itemView.findViewById(R.id.statisticsContent)

        private val totalIssuedTextView: TextView = itemView.findViewById(R.id.totalIssuedTextView)
        private val usedCountTextView: TextView = itemView.findViewById(R.id.usedCountTextView)
        private val remainingCountTextView: TextView = itemView.findViewById(R.id.remainingCountTextView)
        private val expirationDateTextView: TextView = itemView.findViewById(R.id.expirationDateTextView)
        private val unusedUsersTextView: TextView = itemView.findViewById(R.id.unusedUsersTextView)
        private val usedUsersTextView: TextView = itemView.findViewById(R.id.usedUsersTextView)

        fun bind(
            requestedCoupon: RequestedCouponResponse,
            status: CouponStatusResponse?,
            isExpanded: Boolean
        ) {
            businessNameTextView.text = requestedCoupon.business_name
            couponCodeTextView.text = "${itemView.context.getString(R.string.coupon_code)}: ${requestedCoupon.coupon_code ?: "N/A"}"
            statusTextView.text = "${itemView.context.getString(R.string.status)}: ${requestedCoupon.status}"

            statisticsContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE

            if (isExpanded) {
                if (status != null) {
                    progressBar.visibility = View.GONE
                    statisticsContent.visibility = View.VISIBLE

                    totalIssuedTextView.text = "${itemView.context.getString(R.string.total_issued)}: ${status.total_issued}장"
                    usedCountTextView.text = "${itemView.context.getString(R.string.used)}: ${status.used_count}장"
                    remainingCountTextView.text = "${itemView.context.getString(R.string.remaining_count)}: ${status.remaining_count}장"
                    expirationDateTextView.text = "${itemView.context.getString(R.string.expiration_date)}: ${status.expiration_date}"

                    val unusedUsersText = status.unused_users.joinToString("\n") {
                        "${it.email} (${itemView.context.getString(R.string.issued_date)}: ${it.issued_date})"
                    }
                    unusedUsersTextView.text = "${itemView.context.getString(R.string.unused_users)}:\n$unusedUsersText"

                    val usedUsersText = status.used_users.joinToString("\n") {
                        "${it.email} (${itemView.context.getString(R.string.used_date)}: ${it.used_date})"
                    }
                    usedUsersTextView.text = "${itemView.context.getString(R.string.used_users)}:\n$usedUsersText"
                } else {
                    progressBar.visibility = View.VISIBLE
                    statisticsContent.visibility = View.GONE
                }
            } else {
                progressBar.visibility = View.GONE
                statisticsContent.visibility = View.GONE
            }
        }

        fun showLoading(show: Boolean) {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            statisticsContent.visibility = View.GONE
        }
    }
}