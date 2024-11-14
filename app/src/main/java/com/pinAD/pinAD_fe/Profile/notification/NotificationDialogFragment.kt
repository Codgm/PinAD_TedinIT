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
import com.pinAD.pinAD_fe.Profile.SettingFragment
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

        isBusinessUser = arguments?.getBoolean("business_user") ?: true
        (targetFragment as? SettingFragment)?.updateBusinessStatus(isBusinessUser)
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
                        Toast.makeText(context, getString(R.string.empty_notification_list), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("Notification", "Error: ${response.message()} Code: ${response.code()}")
                    Toast.makeText(context, getString(R.string.notification_load_failure), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Notification", "Network or parsing error: ${e.message}")
                Toast.makeText(context, getString(R.string.network_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCouponApproval(requestId: String, action: String) {
        lifecycleScope.launch {
            try {
                val couponResponse = NotificationResBusiness(requestId, action)
                val response = RetrofitInstance.api.approveCouponRequest(couponResponse)
                if (response.isSuccessful) {
                    Toast.makeText(context,  getString(R.string.coupon_approve_success), Toast.LENGTH_SHORT).show()
                    loadNotifications() // 알림 목록 새로고침
                } else {
                    Toast.makeText(context, getString(R.string.coupon_approve_failure), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.network_error), Toast.LENGTH_SHORT).show()
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
                        if (action == "accept") getString(R.string.coupon_response_success_accept)
                        else getString(R.string.coupon_response_success_reject),
                        Toast.LENGTH_SHORT
                    ).show()
                    loadNotifications() // 알림 목록 새로고침
                } else {
                    Toast.makeText(context, getString(R.string.coupon_response_failure), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.network_error), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
