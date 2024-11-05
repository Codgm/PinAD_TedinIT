package com.pinAD.pinAD_fe.Data.pin

data class PinWrapper(
    val pin: FltPinData,
    val media_urls: List<String>,
    val coupon: Coupon
)
