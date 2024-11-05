package com.pinAD.pinAD_fe.Data.coupon

data class CouponResponse(
    val id: Int,
    val coupon: Coupon,
    val qr_code: String
)
