package com.pinAD.pinAD_fe.Profile.coupon

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pinAD.pinAD_fe.Data.coupon.CouponResponse
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.R
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CouponDialogFragment : DialogFragment() {

    private lateinit var recyclerViewCoupons: RecyclerView
    private lateinit var couponAdapter: CouponAdapter
    private lateinit var editTextCouponCode: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_coupons, container, false)

        recyclerViewCoupons = view.findViewById(R.id.recyclerViewCoupons)
        recyclerViewCoupons.layoutManager = LinearLayoutManager(context)

        editTextCouponCode = view.findViewById(R.id.editTextCouponCode)
        val buttonVerifyCoupon = view.findViewById<Button>(R.id.buttonVerifyCoupon)
        val buttonRequestedCoupons = view.findViewById<Button>(R.id.buttonRequestedCoupons)
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

        buttonVerifyCoupon.setOnClickListener {
            val couponCode = editTextCouponCode.text.toString().trim()
            if (couponCode.isNotEmpty()) {
                issueCoupon(couponCode)
            } else {
                Toast.makeText(requireContext(), "쿠폰 코드를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        buttonRequestedCoupons.setOnClickListener {
            // RequestedCouponDialogFragment 호출
            RequestedCouponDialogFragment().show(childFragmentManager, "RequestedCouponDialog")
        }

        editTextCouponCode.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val couponCode = editTextCouponCode.text.toString().trim()
                if (couponCode.isNotEmpty()) {
                    issueCoupon(couponCode)
                } else {
                    Toast.makeText(requireContext(), "쿠폰 코드를 입력하세요.", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        return view
    }

    private fun issueCoupon(couponCode: String) {
        val couponRequest = mapOf("coupon_code" to couponCode)

        val call = RetrofitInstance.api.issueCoupon(couponRequest)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("CouponIssue", "Coupon issued successfully")
                    // 성공 메시지 표시
                    Toast.makeText(context, "쿠폰 발급 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("CouponIssue", "Error issuing coupon: ${response.errorBody()?.string()}")
                    // 오류 메시지 표시
                    Toast.makeText(context, "쿠폰 발급 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("CouponIssue", "Failure: ${t.message}", t)
                // 네트워크 오류 메시지 표시
                Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
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
                    val couponList: List<CouponResponse> = response.body()!! // List<CouponResponse> 형태로 받아오기
                    if (couponList.isNotEmpty()) {
                        couponAdapter.submitCoupons(couponList)
                    } else {
                        // 데이터가 없을 때의 처리 (예: 사용자에게 알림)
                        Toast.makeText(requireContext(), "쿠폰이 없습니다.", Toast.LENGTH_SHORT).show()
                    } // 어댑터에 배열 전달
                } else {
                    handleError(response)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleError(response: Response<List<CouponResponse>>) {
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