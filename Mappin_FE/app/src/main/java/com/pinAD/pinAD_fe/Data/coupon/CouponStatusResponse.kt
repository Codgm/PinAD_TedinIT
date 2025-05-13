package com.pinAD.pinAD_fe.Data.coupon

data class CouponStatusResponse(
    val coupon_code: String,
    val total_issued: Int,
    val used_count: Int,
    val remaining_count: Int,
    val expiration_date: String,
    val unused_users: List<CouponUserInfo>,
    val used_users: List<CouponUserInfo>
)
