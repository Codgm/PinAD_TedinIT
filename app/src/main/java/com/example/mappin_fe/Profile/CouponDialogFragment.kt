package com.example.mappin_fe.Profile

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mappin_fe.Data.CouponResponse
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.R
import kotlinx.coroutines.launch
import retrofit2.Response

class CouponDialogFragment : DialogFragment() {

    private lateinit var recyclerViewCoupons: RecyclerView
    private lateinit var couponAdapter: CouponAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_coupons, container, false)

        recyclerViewCoupons = view.findViewById(R.id.recyclerViewCoupons)
        recyclerViewCoupons.layoutManager = LinearLayoutManager(context)

        couponAdapter = CouponAdapter { couponResponse ->
            showQrCodeDialog(decodeBase64ToBitmap(couponResponse.qr_code))
        }
        recyclerViewCoupons.adapter = couponAdapter

        // 쿠폰 데이터 가져오기
        val accessToken = RetrofitInstance.getAccessToken()
        if (accessToken != null) {
            fetchCoupons(accessToken)
        } else {
            Toast.makeText(requireContext(), "인증되지 않은 사용자입니다.", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return dialog
    }

    private fun fetchCoupons(accessToken: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.showCoupons("Bearer $accessToken")
                if (response.isSuccessful && response.body() != null) {
                    val couponResponse = response.body() // 단일 CouponResponse 객체로 받아오기
                    if (couponResponse != null) {
                        couponAdapter.submitCoupon(couponResponse)
                    }
                } else {
                    handleError(response)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleError(response: Response<CouponResponse>) {
        when (response.code()) {
            404 -> Toast.makeText(requireContext(), "쿠폰을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(requireContext(), "알 수 없는 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decodeBase64ToBitmap(base64: String): Bitmap {
        val decodedString = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    private fun showQrCodeDialog(qrCodeBitmap: Bitmap) {
        val qrCodeDialog = QrCodeDialogFragment(qrCodeBitmap)
        qrCodeDialog.show(childFragmentManager, "QrCodeDialog")
    }
}