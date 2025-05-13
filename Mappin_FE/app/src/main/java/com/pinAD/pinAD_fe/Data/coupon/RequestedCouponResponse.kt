package com.pinAD.pinAD_fe.Data.coupon

data class RequestedCouponResponse(
    val response_id: Int,
    val coupon_request_id: Int,
    val user_name: String,
    val business_name: String,
    val coupon_code: String,
    val status: String,
    val responded_at: String?,
    val created_at: String
)
