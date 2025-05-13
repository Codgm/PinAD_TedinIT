package com.pinAD.pinAD_fe.Data.business

data class BusinessCreateRequest(
    val businessName: String,
    val businessType: String,
    val address: String,
    val phone: String,
    val email: String,
    val latitude: Double,
    val longitude: Double
)
