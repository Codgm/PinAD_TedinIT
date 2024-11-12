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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.pinAD.pinAD_fe.Data.pin.FltPinData
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.Profile.ProfileFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.pinAD.pinAD_fe.Data.coupon.Coupon
import com.pinAD.pinAD_fe.Data.pin.like_comment.Comment
import com.pinAD.pinAD_fe.Data.pin.like_comment.LikeResponse
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class PinDetailBottomSheet : BottomSheetDialogFragment() {
    private var pinId: Int = 0
    private var isLiked: Boolean = false
    private var totalLikes: Int = 0
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var tvDiscountAmount: TextView
    val accessToken = RetrofitInstance.getAccessToken()

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

        try {
            arguments?.let { bundle ->
                val pinJson = bundle.getString("pinData")
                val mediaUrlsJson = bundle.getString("mediaUrls")
                val couponJson = bundle.getString("coupon")

                if (!pinJson.isNullOrEmpty() && !mediaUrlsJson.isNullOrEmpty()) {
                    val gson = GsonBuilder()
                        .registerTypeAdapter(List::class.java, TagsDeserializer())
                        .create()

                    try {
                        val pinData = gson.fromJson(pinJson, FltPinData::class.java)
                        val mediaUrls = gson.fromJson(mediaUrlsJson, Array<String>::class.java).toList()

                        // coupon은 pin_type이 0또는 1일 때만 파싱
                        val coupon = if (!couponJson.isNullOrEmpty() && (pinData.pin_type == 0 || pinData.pin_type == 1)) {
                            gson.fromJson(couponJson, Coupon::class.java)
                        } else null

                        pinId = pinData.id
                        setupUI(view, pinData, mediaUrls, coupon)

                        // visibility가 public일 때만 좋아요와 댓글 UI 설정
                        if (pinData.visibility == "public") {
                            setupLikeButton(view)
                            setupComments(view)
                            loadComments()
                        } else {
                            // private인 경우 좋아요와 댓글 UI 숨기기
                            hideInteractionUI(view)
                        }
                        setupMoreDetailsButton(view, pinData, mediaUrls)

                    } catch (e: Exception) {
                        Log.e("PinDetailBottomSheet", "Error parsing JSON: ${e.message}", e)
                        Toast.makeText(context, "데이터 로딩 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } ?: run {
                Log.e("PinDetailBottomSheet", "No arguments provided")
                Toast.makeText(context, "데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("PinDetailBottomSheet", "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideInteractionUI(view: View) {
        // 좋아요 관련 UI 숨기기
        view.findViewById<ImageButton>(R.id.btnLike)?.visibility = View.GONE
        view.findViewById<TextView>(R.id.tvLikeCount)?.visibility = View.GONE

        // 댓글 관련 UI 숨기기
        view.findViewById<RecyclerView>(R.id.rvComments)?.visibility = View.GONE
        view.findViewById<EditText>(R.id.etComment)?.visibility = View.GONE
        view.findViewById<ImageButton>(R.id.btnSendComment)?.visibility = View.GONE
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI(view: View, pinData: FltPinData, mediaUrls: List<String>, coupon: Coupon?) {
        view.findViewById<TextView>(R.id.tvTitle).text = pinData.title
        view.findViewById<TextView>(R.id.tvDescription).text = "설명: ${pinData.description}"
        tvDiscountAmount = view.findViewById(R.id.tvDiscountAmount)
        val infoJson = JSONObject(pinData.info.toString())

        if (pinData.pin_type == 0 || pinData.pin_type == 1) {
            val discountAmount = coupon?.discount_amount
            tvDiscountAmount.text =
                "할인: ${discountAmount} ${infoJson.optString("discount_type", "N/A")}"
        } else {
            tvDiscountAmount.visibility = View.GONE
        }

        val durationInHours = convertDurationToMilliseconds(pinData.duration)
        Log.d("DurationConversion", "Converted duration to milliseconds: $durationInHours")

        // 버튼 및 UI 요소들의 가시성을 pin_type에 따라 설정
        val btnIssueCoupon = view.findViewById<Button>(R.id.btnIssueCoupon)
        val btnApproveRequest = view.findViewById<Button>(R.id.btnApproveRequest)
        val progressBarLayout = view.findViewById<View>(R.id.progressBarLayout)

        // UI 요소들 가시성 초기화
        btnIssueCoupon.visibility = View.GONE
        btnApproveRequest.visibility = View.GONE
        progressBarLayout.visibility = View.GONE

        // pin_type에 따른 UI 설정
        when (pinData.pin_type) {
            0 -> { // 쿠폰 발급
                btnIssueCoupon.visibility = View.VISIBLE
                progressBarLayout.visibility = View.VISIBLE
                coupon?.let {
                    btnIssueCoupon.setOnClickListener {
                        issueCoupon(coupon.code)
                    }
                    setupProgressBar(view, it)
                }
            }
            1 -> { // 승인 요청
                btnApproveRequest.visibility = View.VISIBLE
                progressBarLayout.visibility = View.VISIBLE
                coupon?.let {
                    setupProgressBar(view, it)
                }
            }
            2 -> { // 일반 핀
                // 모든 쿠폰 관련 UI는 이미 GONE으로 설정됨
            }
        }

        setupChronometer(view, pinData, pinData.duration_milliseconds)
    }

    private fun setupLikeButton(view: View) {
        val btnLike: ImageButton = view.findViewById(R.id.btnLike)
        val tvLikeCount = view.findViewById<TextView>(R.id.tvLikeCount)

        btnLike.setOnClickListener {
            val call = RetrofitInstance.api.likePin(pinId)
            call.enqueue(object : Callback<LikeResponse> {
                override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { likeResponse ->
                            isLiked = likeResponse.liked
                            totalLikes = likeResponse.total_likes
                            updateLikeUI(btnLike, tvLikeCount)
                        }
                    } else {
                        Toast.makeText(context, "Failed to update like", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                    Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateLikeUI(btnLike: ImageButton, tvLikeCount: TextView) {
        btnLike.setImageResource(
            if (isLiked) R.drawable.ic_liked else R.drawable.ic_like
        )
        tvLikeCount.text = totalLikes.toString()
    }

    private fun setupComments(view: View) {
        val rvComments = view.findViewById<RecyclerView>(R.id.rvComments)
        val etComment = view.findViewById<EditText>(R.id.etComment)
        val btnSendComment: ImageButton = view.findViewById(R.id.btnSendComment)

        commentsAdapter = CommentsAdapter(mutableListOf()) { commentId ->
            deleteComment(commentId)
        }

        rvComments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentsAdapter
        }

        btnSendComment.setOnClickListener {
            val content = etComment.text.toString().trim()
            if (content.isNotEmpty()) {
                addComment(content)
                etComment.text.clear()
            }
        }
    }

    private fun loadComments() {
        val call = RetrofitInstance.api.getComments("Bearer $accessToken", pinId)
        call.enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    response.body()?.let { comments ->
                        commentsAdapter.updateComments(comments)
                    }
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                Toast.makeText(context, "Failed to load comments", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addComment(content: String) {
        val call = RetrofitInstance.api.addComment(pinId, mapOf("content" to content))
        call.enqueue(object : Callback<Comment> {
            override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                if (response.isSuccessful) {
                    response.body()?.let { comment ->
                        commentsAdapter.addComment(comment)
                    }
                } else {
                    Toast.makeText(context, "Failed to add comment", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Comment>, t: Throwable) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteComment(commentId: Int) {
        val call = RetrofitInstance.api.deleteComment(commentId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    commentsAdapter.removeComment(commentId)
                } else {
                    Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun convertDurationToMilliseconds(duration: String): Long {
        return try {
            when {
                duration.contains(" ") -> {
                    // 1 00:00:00 형식 (일 시:분:초)
                    val parts = duration.split(" ")
                    val days = parts[0].toLong()
                    val timeParts = parts[1].split(":").map { it.toLong() }

                    val totalSeconds = (days * 24 * 3600) + // 일을 초로
                            (timeParts[0] * 3600) + // 시를 초로
                            (timeParts[1] * 60) +   // 분을 초로
                            timeParts[2]            // 초

                    totalSeconds * 1000 // 밀리초로 변환
                }
                duration.contains(":") -> {
                    // 16:00:00 형식 (시:분:초)
                    val timeParts = duration.split(":").map { it.toLong() }
                    val totalSeconds = (timeParts[0] * 3600) + // 시를 초로
                            (timeParts[1] * 60) +     // 분을 초로
                            timeParts[2]              // 초

                    totalSeconds * 1000 // 밀리초로 변환
                }
                else -> 0L
            }
        } catch (e: Exception) {
            Log.e("DurationConversion", "Error converting duration: ${e.message}", e)
            0L
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupChronometer(view: View, pinData: FltPinData, durationMilliseconds: Int) {
        val chronometer = view.findViewById<Chronometer>(R.id.chronometer)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")

        // UTC로 설정된 created_at 시간을 한국 시간으로 변환
        val utcCreatedAt = pinData.created_at.toInstant()

        // 현재 시간을 한국 시간으로 가져오기
        val nowInKST = Instant.now().plus(9, ChronoUnit.HOURS)

        // 종료 시간 역시 한국 시간대로 계산
        val endTimeKST = utcCreatedAt.plusMillis(durationMilliseconds.toLong())

        // 상세 로깅
        Log.d("TimeDebug", "Created at (KST): ${utcCreatedAt}")
        Log.d("TimeDebug", "End time (KST): ${endTimeKST}")
        Log.d("TimeDebug", "Duration set: ${durationMilliseconds / 1000 / 60 / 60} hours")

        // 남은 시간을 KST 기준으로 계산
        val remainingTime = Duration.between(nowInKST, endTimeKST).toMillis()
        Log.d("TimeDebug", "Remaining time: $remainingTime ms (${remainingTime / 1000 / 60 / 60} hours)")

        if (remainingTime > 0) {
            chronometer.base = SystemClock.elapsedRealtime() + remainingTime
            chronometer.start()

            chronometer.onChronometerTickListener = Chronometer.OnChronometerTickListener {
                val elapsedMillis = chronometer.base - SystemClock.elapsedRealtime()
                if (elapsedMillis <= 0) {
                    chronometer.stop()
                    chronometer.text = "Expired"
                }
            }
        } else {
            chronometer.text = "Expired"
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

    private fun setupProgressBar(view: View, coupon: Coupon) {
        try {
            val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
            val tvParticipantInfo = view.findViewById<TextView>(R.id.tvParticipantInfo)

            val maxParticipants = coupon.max_issued_count
            val currentParticipants = coupon.current_issued_count
            val remain = maxParticipants - currentParticipants

            progressBar.max = maxParticipants
            progressBar.progress = remain

            tvParticipantInfo.text = "Current participants: $currentParticipants / $maxParticipants"
        } catch (e: Exception) {
            Log.e("ProgressBar", "Error setting up progress bar: ${e.message}")
        }
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
        val interactionLayout = view.findViewById<ConstraintLayout>(R.id.interactionLayout)

        // Initialize RecyclerView
        recyclerViewMedia.layoutManager = GridLayoutManager(view.context, 3)

        // Set adapter with mediaUrls if not empty
        if (mediaUrls.isNotEmpty()) {
            recyclerViewMedia.adapter = MediaAdapter(mediaUrls)
        }

        var isExpanded = false

        interactionLayout.visibility = View.GONE

        tvMoreDetails.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                interactionLayout.visibility = View.VISIBLE
                contentLayout.visibility = View.VISIBLE
                recyclerViewMedia.visibility = View.VISIBLE
                tvMoreDetails.text = "접기"

                val behavior = BottomSheetBehavior.from(view.parent as View)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                interactionLayout.visibility = View.GONE
                contentLayout.visibility = View.GONE
                recyclerViewMedia.visibility = View.GONE
                tvMoreDetails.text = "자세히 보기"

                // BottomSheet를 중간 크기로 변경
                val behavior = BottomSheetBehavior.from(view.parent as View)
                behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
            val infoJson = JSONObject(pinData.info.toString())

//            val infoJson = try {
//                JSONObject(pinData.info.toString())
//            } catch (e: Exception) {
//                Log.e("PinDetailBottomSheet", "Error parsing info JSON: ${e.message}", e)
//                JSONObject()
//            }
//            val additionalInfo = infoJson.optString("additionalInfo", "{}")
//            val additionalInfoJson = try {
//                JSONObject(additionalInfo)
//            } catch (e: Exception) {
//                Log.e("PinDetailBottomSheet", "Error parsing additionalInfo JSON: ${e.message}", e)
//                JSONObject()
//            }

            if (pinData.pin_type == 0) {
                tvField1.text = "상품명 : ${infoJson.optString("product_name", "N/A")}"
                tvField1.visibility = View.VISIBLE
                tvField2.visibility = View.VISIBLE
                tvField3.visibility = View.VISIBLE
            } else if (pinData.pin_type == 1) {
                tvField1.text = "요청 사유 : ${infoJson.optString("product_name", "N/A")}"
                tvField1.visibility = View.VISIBLE
                tvField2.visibility = View.GONE
                tvField3.visibility = View.GONE
            } else {
                tvField1.text = "장점 : ${infoJson.optString("advantages", "N/A")}"
                tvField2.text = "단점 : ${infoJson.optString("disadvantages", "N/A")}"
                tvField1.visibility = View.VISIBLE
                tvField2.visibility = View.VISIBLE
                tvField3.visibility = View.GONE
            }

            if (pinData.visibility == "public") {
                interactionLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            }
        }
    }
}
