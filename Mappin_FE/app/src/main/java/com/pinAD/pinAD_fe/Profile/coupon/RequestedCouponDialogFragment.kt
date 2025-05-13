// RequestedCouponDialogFragment.kt

package com.pinAD.pinAD_fe.Profile.coupon

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pinAD.pinAD_fe.Data.coupon.RequestedCouponResponse
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.R
import kotlinx.coroutines.launch
import retrofit2.Response

class RequestedCouponDialogFragment : DialogFragment() {

    private lateinit var recyclerViewCoupons: RecyclerView
    private lateinit var couponAdapter: RequestedCouponAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_requested_coupons, container, false)

        recyclerViewCoupons = view.findViewById(R.id.recyclerViewCoupons)
        recyclerViewCoupons.layoutManager = LinearLayoutManager(context)

        couponAdapter = RequestedCouponAdapter(viewLifecycleOwner.lifecycleScope)
        recyclerViewCoupons.adapter = couponAdapter

        // 쿠폰 데이터 가져오기
        val accessToken = RetrofitInstance.getAccessToken()
        if (accessToken != null) {
            fetchCoupons(accessToken)
        } else {
            Toast.makeText(requireContext(), getString(R.string.unauthenticated_user), Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun fetchCoupons(accessToken: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.showRequestedCoupons("Bearer $accessToken")
                if (response.isSuccessful && response.body() != null) {
                    val couponList: List<RequestedCouponResponse> = response.body()!!
                    if (couponList.isNotEmpty()) {
                        couponAdapter.submitRequestedCoupons(couponList)
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.no_requested_coupons), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    handleError(response)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleError(response: Response<List<RequestedCouponResponse>>) {
        when (response.code()) {
            404 -> Toast.makeText(requireContext(), getString(R.string.coupon_not_found), Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(requireContext(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return dialog
    }
}
