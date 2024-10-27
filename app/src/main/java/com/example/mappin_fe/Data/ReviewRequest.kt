package com.example.mappin_fe.Data

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ReviewRequest(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val Info: Any?, // JSON 형태의 추가 정보
    val tags: List<String>,
    val latitude: Double,
    val longitude: Double,
    val points: Int,
    val isAds: Boolean,
    val mediaFiles: List<String>,
    val created_at: Date,
)
