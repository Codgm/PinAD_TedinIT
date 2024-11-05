package com.pinAD.pinAD_fe.Data.pin

import com.google.gson.annotations.SerializedName

data class Coupon(
    @SerializedName("discount_amount") val discount_amount: Int,
    @SerializedName("max_issued_count") val max_issued_count: Int,
    @SerializedName("code") val code: String,
)
