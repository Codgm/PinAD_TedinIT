package com.pinAD.pinAD_fe

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pinAD.pinAD_fe.Data.pin.FltPinData
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.Profile.ProfileFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.pinAD.pinAD_fe.Data.coupon.Coupon
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class PinDetailBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(pinJson: String, mediaUrlsJson: String, couponJson: String): PinDetailBottomSheet {
            val args = Bundle().apply {
                putString("pinData", pinJson)
                putString("mediaUrls", mediaUrlsJson)
                putString("coupon", couponJson)
            }
            return PinDetailBottomSheet().apply {
                arguments = args
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_pin_detail, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            val pinJson = bundle.getString("pinData")
            val mediaUrlsJson = bundle.getString("mediaUrls")
            val couponJson = bundle.getString("coupon")

            if (pinJson != null && mediaUrlsJson != null) {
                val gson = GsonBuilder()
                    .registerTypeAdapter(List::class.java, TagsDeserializer())
                    .create()
                // JSON 데이터를 FltPinData 및 mediaUrls 리스트로 변환
                val pinData = gson.fromJson(pinJson, FltPinData::class.java)
                val mediaUrls = gson.fromJson(mediaUrlsJson, Array<String>::class.java).toList()
                val coupon = gson.fromJson(couponJson, Coupon::class.java)

                // UI 업데이트
                setupUI(view, pinData, mediaUrls, coupon)
            } else {
                // 예외 처리: 데이터가 없을 때
                Log.e("PinDetailBottomSheet", "Error parsing data", )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI(view: View, pinData: FltPinData, mediaUrls: List<String>, coupon: Coupon) {
        view.findViewById<TextView>(R.id.tvTitle).text = pinData.title
        view.findViewById<TextView>(R.id.tvDescription).text = "${pinData.description}"

        val durationInHours = convertDurationToHours(pinData.duration)
        Log.d("DurationConversion", "Converted duration to milliseconds: $durationInHours")

        view.findViewById<TextView>(R.id.tvRange).visibility = View.GONE
        view.findViewById<TextView>(R.id.tvDuration).apply {
            text = "Duration: $durationInHours seconds"
            visibility = if (durationInHours > 0) View.VISIBLE else View.GONE
        }

        val btnIssueCoupon = view.findViewById<Button>(R.id.btnIssueCoupon)
        // `pinWrapper.pin.pin_type`을 확인하여 버튼 가시성 설정
        btnIssueCoupon.visibility = if (pinData.pin_type == 0) View.VISIBLE else View.GONE
        btnIssueCoupon.setOnClickListener {
            issueCoupon(coupon.code) // `coupon`의 `code` 사용
        }

        val btnApproveRequest = view.findViewById<Button>(R.id.btnApproveRequest)
        btnApproveRequest.visibility = if (pinData.pin_type == 1) View.VISIBLE else View.GONE

        setupChronometer(view, pinData, durationInHours)
        setupProgressBar(view, coupon)
        setupMoreDetailsButton(view, pinData, mediaUrls)
    }

    private fun convertDurationToHours(duration: String): Long {
        return try {
            if (duration.contains(" ")) {
                val parts = duration.split(" ")
                val days = parts[0].toInt()
                val timeParts = parts[1].split(":").map { it.toInt() }
                // 일, 시, 분, 초를 밀리초로 변환
                ((days * 24 * 3600 + timeParts[0] * 3600 + timeParts[1] * 60 + timeParts[2]) * 1000).toLong()
            } else {
                val parts = duration.split(":").map { it.toInt() }
                if (parts.size == 3) {
                    // 시, 분, 초를 밀리초로 변환
                    ((parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000).toLong()
                } else {
                    0L
                }
            }
        } catch (e: Exception) {
            Log.e("DurationConversion", "Error converting duration: ${e.message}", e)
            0L
        }
    }



    private class TagsDeserializer : JsonDeserializer<List<Any>> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List<Any> {
            if (json.isJsonObject) {
                val tagsObject = json.asJsonObject
                if (tagsObject.has("values") && tagsObject.get("values").isJsonArray) {
                    return context.deserialize(tagsObject.get("values"), typeOfT)
                }
            }
            return emptyList() // 또는 다른 기본값 반환
        }
    }

//    private fun setupTags(view: View, pinData: PinDataResponse) {
//        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupTags)
//        pinData.tags.forEach { tag ->
//            val chip = Chip(context)
//            chip.text = tag
//            chipGroup.addView(chip)
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupChronometer(view: View, pinData: FltPinData, duration: Long) {
        val chronometer = view.findViewById<Chronometer>(R.id.chronometer)
        val endTime = pinData.created_at.time + duration // duration은 이제 밀리초 단위입니다.
        Log.d("ChronometerSetup", "Created at: ${pinData.created_at}, Timestamp: ${pinData.created_at.time}")
        val remainingTime = endTime - System.currentTimeMillis()
        Log.d("ChronometerSetup", "End time: $endTime, Current time: ${System.currentTimeMillis()}")
        Log.d("ChronometerSetup", "Remaining time: $remainingTime")

        if (remainingTime > 0) {
            chronometer.base = SystemClock.elapsedRealtime() + remainingTime
            Log.d("ChronometerSetup", "Chronometer started with base: ${chronometer.base}")
            chronometer.start()
        } else {
            chronometer.text = "Expired"
            Log.d("ChronometerSetup", "Chronometer expired")
        }
    }

    private fun setupProgressBar(view: View, coupon: Coupon) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val maxParticipants = coupon.max_issued_count
        val currentParticipants = coupon.discount_amount
        progressBar.max = maxParticipants
        progressBar.progress = currentParticipants

        view.findViewById<TextView>(R.id.tvParticipantInfo).text =
            "Current participants: $currentParticipants / $maxParticipants"
    }

//    private fun approveCouponRequest(pinId: String) {
//        val approvalRequest = mapOf("pin_id" to pinId)
//
//        val call = RetrofitInstance.api.approveCouponRequest(approvalRequest)
//        call.enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful) {
//                    Log.d("CouponApproval", "Coupon request approved successfully")
//                    Toast.makeText(context, "쿠폰 요청이 승인되었습니다.", Toast.LENGTH_SHORT).show()
//                    navigateToProfileFragment() // 승인 후 사용자 프로필로 이동
//                } else {
//                    Log.e("CouponApproval", "Error approving coupon request: ${response.errorBody()?.string()}")
//                    Toast.makeText(context, "쿠폰 승인 실패", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                Log.e("CouponApproval", "Failure: ${t.message}", t)
//                Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

    private fun issueCoupon(couponCode: String) {
        val couponRequest = mapOf("coupon_code" to couponCode)

        val call = RetrofitInstance.api.issueCoupon(couponRequest)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("CouponIssue", "Coupon issued successfully")
                    // 성공 메시지 표시
                    Toast.makeText(context, "쿠폰 발급 성공", Toast.LENGTH_SHORT).show()
                    navigateToProfileFragment()
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

    private fun navigateToProfileFragment() {
        val profileFragment = ProfileFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_body_container, profileFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupMoreDetailsButton(view: View, pinData: FltPinData, mediaUrls: List<String>) {
        val tvMoreDetails = view.findViewById<TextView>(R.id.tvMoreDetails)
        val recyclerViewMedia = view.findViewById<RecyclerView>(R.id.recyclerViewMedia)
        val contentLayout = view.findViewById<ConstraintLayout>(R.id.contentLayout)
        val tvField1 = view.findViewById<TextView>(R.id.tvField1)
        val tvField2 = view.findViewById<TextView>(R.id.tvField2)
        val tvField3 = view.findViewById<TextView>(R.id.tvField3)

        // Initialize RecyclerView
        recyclerViewMedia.layoutManager = LinearLayoutManager(view.context)

        // Set adapter with mediaUrls if not empty
        if (mediaUrls.isNotEmpty()) {
            recyclerViewMedia.adapter = MediaAdapter(mediaUrls)
        }

        var isExpanded = false

        tvMoreDetails.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                contentLayout.visibility = View.VISIBLE
                recyclerViewMedia.visibility = View.VISIBLE
                tvMoreDetails.text = "접기"
            } else {
                contentLayout.visibility = View.GONE
                recyclerViewMedia.visibility = View.GONE
                tvMoreDetails.text = "자세히 보기"
            }

            val infoJson = try {
                JSONObject(pinData.info.toString())
            } catch (e: Exception) {
                Log.e("PinDetailBottomSheet", "Error parsing info JSON: ${e.message}", e)
                JSONObject()
            }
            val additionalInfo = infoJson.optString("additionalInfo", "{}")
            val additionalInfoJson = try {
                JSONObject(additionalInfo)
            } catch (e: Exception) {
                Log.e("PinDetailBottomSheet", "Error parsing additionalInfo JSON: ${e.message}", e)
                JSONObject()
            }

            if (pinData.is_ads == true) {
                tvField1.text = "상품명 : ${additionalInfoJson.optString("field1", "N/A")}"
                tvField2.text = "판매 수량 : ${additionalInfoJson.optString("field2", "N/A")}"
                tvField3.text = "할인율 또는 할인가 : ${additionalInfoJson.optString("field3", "N/A")}"
                tvField1.visibility = View.VISIBLE
                tvField2.visibility = View.VISIBLE
                tvField3.visibility = View.VISIBLE
            } else {
                tvField1.text = "요청 사유 : ${additionalInfoJson.optString("field1", "N/A")}"
                tvField1.visibility = View.VISIBLE
                tvField2.visibility = View.GONE
                tvField3.visibility = View.GONE
            }
        }
    }
}
