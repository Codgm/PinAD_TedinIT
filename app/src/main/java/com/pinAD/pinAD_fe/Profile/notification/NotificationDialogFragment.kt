package com.pinAD.pinAD_fe.Profile.notification

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pinAD.pinAD_fe.Data.notification.NotificationResBusiness
import com.pinAD.pinAD_fe.Data.notification.NotificationResponse
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.network.RetrofitInstance
import kotlinx.coroutines.launch

class NotificationDialogFragment : DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private var isBusinessUser: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isBusinessUser = arguments?.getBoolean("business_user") ?: false
        recyclerView = view.findViewById(R.id.recyclerViewNotifications)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = NotificationAdapter(
            onAcceptClick = { notificationId ->
                if (isBusinessUser) {
                    handleCouponApproval(notificationId, "accept")
                } else {
                    handleCouponResponse(notificationId, "accept")
                }
            },
            onRejectClick = { notificationId -> handleCouponResponse(notificationId, "reject") }
        )
        recyclerView.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            try {
                val response = if (isBusinessUser) {
                    RetrofitInstance.api.getBusinessUserNotifications()
                } else {
                    RetrofitInstance.api.getUserNotifications()
                }
                Log.d("Notification", "Response: ${response.body()}")
                if (response.isSuccessful) {
                    val notifications = response.body()
                    if (notifications != null) {
                        adapter.submitList(notifications, isBusinessUser)
                    } else {
                        Toast.makeText(context, "알림 목록이 비어있습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("Notification", "Error: ${response.message()} Code: ${response.code()}")
                    Toast.makeText(context, "알림을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Notification", "Network or parsing error: ${e.message}")
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCouponApproval(requestId: String, action: String) {
        lifecycleScope.launch {
            try {
                val couponResponse = NotificationResBusiness(requestId, action)
                val response = RetrofitInstance.api.approveCouponRequest(couponResponse)
                if (response.isSuccessful) {
                    Toast.makeText(context, "쿠폰 요청이 승인되었습니다.", Toast.LENGTH_SHORT).show()
                    loadNotifications() // 알림 목록 새로고침
                } else {
                    Toast.makeText(context, "쿠폰 승인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCouponResponse(notificationId: String, action: String) {
        lifecycleScope.launch {
            try {
                val couponResponse = NotificationResponse(notificationId, action)
                val response = RetrofitInstance.api.respondToCoupon(couponResponse)
                if (response.isSuccessful) {
                    Toast.makeText(context,
                        if (action == "accept") "쿠폰을 수락했습니다."
                        else "쿠폰을 거절했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadNotifications() // 알림 목록 새로고침
                } else {
                    Toast.makeText(context, "쿠폰 응답에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
