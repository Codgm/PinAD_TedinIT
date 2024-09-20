package com.example.mappin_fe.Data

import java.util.Date

data class PinDataResponse(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val location: String,
    val user: Int,
    val title: String,
    val description: String,
    val range: Int,
    val duration: Int,
    val mainCategory: String,
    val subCategory: String,
    val mediafiles: List<String>,
    val info: String,
    val tags: List<String>,
    val visibility: String,
    val is_ads: Boolean,
    val created_at: Date,
    val updated_at: Date
)
