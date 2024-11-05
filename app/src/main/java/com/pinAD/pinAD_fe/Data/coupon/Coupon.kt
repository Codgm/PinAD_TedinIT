package com.pinAD.pinAD_fe.Data.coupon

data class Coupon(
    val code: String,
    val discount_amount: Int,
    val max_issued_count: Int
)
