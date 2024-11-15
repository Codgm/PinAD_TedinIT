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

    companion object {
        private const val ARG_IS_BUSINESS_USER = "business_user"

        fun newInstance(isBusinessUser: Boolean): NotificationDialogFragment {
            val fragment = NotificationDialogFragment()
            val args = Bundle().apply {
                putBoolean(ARG_IS_BUSINESS_USER, isBusinessUser)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isBusinessUser = arguments?.getBoolean(ARG_IS_BUSINESS_USER, false) ?: false
        Log.d("NotificationDialog", "Received isBusinessUser: $isBusinessUser")

        setupRecyclerView(view)
        loadNotifications()
    }

    private fun setupRecyclerView(view: View) {
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
            onRejectClick = { notificationId ->
                if (isBusinessUser) {
                    handleCouponApproval(notificationId, "reject")
                } else {
                    handleCouponResponse(notificationId, "reject")
                }
            }
        )

        recyclerView.adapter = adapter
    }


    private fun loadNotifications() {
        Log.d("NotificationDialog", "Loading notifications. isBusinessUser: $isBusinessUser")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = if (isBusinessUser) {
                    Log.d("NotificationDialog", "Requesting business notifications")
                    RetrofitInstance.api.getBusinessUserNotifications()
                } else {
                    Log.d("NotificationDialog", "Requesting user notifications")
                    RetrofitInstance.api.getUserNotifications()
                }

                Log.d("NotificationDialog", "Response received: ${response.code()}")
                if (response.isSuccessful) {
                    val notifications = response.body()
                    if (notifications != null) {
                        adapter.submitList(notifications, isBusinessUser)
                        Log.d("NotificationDialog", "Notifications loaded successfully")
                    } else {
                        showToast(getString(R.string.empty_notification_list))
                    }
                } else {
                    Log.e("NotificationDialog", "Error: ${response.message()} Code: ${response.code()}")
                    showToast(getString(R.string.notification_load_failure))
                }
            } catch (e: Exception) {
                Log.e("NotificationDialog", "Network or parsing error", e)
                showToast(getString(R.string.network_error))
            }
        }
    }

    private fun handleCouponApproval(requestId: String, action: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val couponResponse = NotificationResBusiness(requestId, action)
                val response = RetrofitInstance.api.approveCouponRequest(couponResponse)

                if (response.isSuccessful) {
                    showToast(
                        if (action == "accept") getString(R.string.coupon_approve_success)
                        else getString(R.string.coupon_approve_failure)
                    )
                    loadNotifications()
                } else {
                    showToast(getString(R.string.coupon_approve_failure))
                }
            } catch (e: Exception) {
                Log.e("NotificationDialog", "Error in handleCouponApproval", e)
                showToast(getString(R.string.network_error))
            }
        }
    }

    private fun handleCouponResponse(notificationId: String, action: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val couponResponse = NotificationResponse(notificationId, action)
                val response = RetrofitInstance.api.respondToCoupon(couponResponse)

                if (response.isSuccessful) {
                    showToast(
                        if (action == "accept") getString(R.string.coupon_response_success_accept)
                        else getString(R.string.coupon_response_success_reject)
                    )
                    loadNotifications()
                } else {
                    showToast(getString(R.string.coupon_response_failure))
                }
            } catch (e: Exception) {
                Log.e("NotificationDialog", "Error in handleCouponResponse", e)
                showToast(getString(R.string.network_error))
            }
        }
    }

    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }
}

