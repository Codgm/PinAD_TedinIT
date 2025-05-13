package com.pinAD.pinAD_fe.Data.coupon

data class CouponUserInfo(
    val email: String,
    val issued_date: String? = null,
    val used_date: String? = null
)
