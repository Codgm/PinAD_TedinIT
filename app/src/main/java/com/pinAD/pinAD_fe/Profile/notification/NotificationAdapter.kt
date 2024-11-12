package com.pinAD.pinAD_fe.Profile.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pinAD.pinAD_fe.Data.notification.BaseNotification
import com.pinAD.pinAD_fe.Data.notification.BusinessNotification
import com.pinAD.pinAD_fe.Data.notification.Notification
import com.pinAD.pinAD_fe.R
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(
    private val onAcceptClick: (String) -> Unit,
    private val onRejectClick: (String) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    private var notifications: List<BaseNotification> = emptyList()
    private var isBusinessUser: Boolean = false

    fun submitList(newList: List<Any>?, isBusinessUser: Boolean) {
        notifications = newList?.map { it as BaseNotification } ?: emptyList()
        this.isBusinessUser = isBusinessUser
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view, onAcceptClick, onRejectClick)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position], isBusinessUser)
    }

    override fun getItemCount() = notifications.size

    class NotificationViewHolder(
        itemView: View,
        private val onAcceptClick: (String) -> Unit,
        private val onRejectClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleView = itemView.findViewById<TextView>(R.id.tvNotificationTitle)
        private val contentView = itemView.findViewById<TextView>(R.id.tvNotificationContent)
        private val dateView = itemView.findViewById<TextView>(R.id.tvNotificationDate)
        private val statusView = itemView.findViewById<TextView>(R.id.tvNotificationStatus)
        private val acceptButton = itemView.findViewById<Button>(R.id.btnAccept)
        private val rejectButton = itemView.findViewById<Button>(R.id.btnReject)

        fun bind(notification: BaseNotification, isBusinessUser: Boolean) {
            titleView.text = notification.title
            contentView.text = when (notification) {
                is BusinessNotification -> notification.content
                is Notification -> with(notification.content) {
                    "상품명: $product_name\n할인율: $discount_type $discount_amount\n유효기간: $valid_until"
                }
                else -> ""
            }
            statusView.text = notification.status

            // 날짜 포맷팅
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            try {
                val date = parser.parse(notification.created_at)
                dateView.text = formatter.format(date)
            } catch (e: Exception) {
                dateView.text = notification.created_at
            }

            val showButtons = when {
                // 비즈니스 사용자이고 쿠폰 요청 알림인 경우
                isBusinessUser && notification.status == "NEW_COUPON_REQUEST" -> true
                // 일반 사용자이고 승인된 쿠폰 알림인 경우
                !isBusinessUser && notification.title.contains("쿠폰 발급") && notification.status == "PENDING" -> true
                else -> false
            }

            if (showButtons) {
                acceptButton.visibility = View.VISIBLE
                rejectButton.visibility = View.VISIBLE

                acceptButton.setOnClickListener { onAcceptClick(notification.notification_id.toString()) }
                rejectButton.setOnClickListener { onRejectClick(notification.notification_id.toString()) }
            } else {
                acceptButton.visibility = View.GONE
                rejectButton.visibility = View.GONE
            }
        }
    }
}