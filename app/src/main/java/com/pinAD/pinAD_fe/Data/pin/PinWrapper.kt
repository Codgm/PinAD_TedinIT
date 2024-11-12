package com.pinAD.pinAD_fe.Data.pin

import com.pinAD.pinAD_fe.Data.coupon.Coupon

data class PinWrapper(
    val pin: FltPinData,
    val media_urls: List<String>,
    val coupon: Coupon
)
