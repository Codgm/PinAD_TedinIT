package com.pinAD.pinAD_fe.Data.coupon

import com.google.gson.annotations.SerializedName

data class Coupon(
    @SerializedName("code") val code: String,
    @SerializedName("discount_amount") val discount_amount: Int,
    @SerializedName("max_issued_count") val max_issued_count: Int,
    @SerializedName("current_issued_count") val current_issued_count: Int
)
