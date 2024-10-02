package com.example.mappin_fe.Data

import com.google.gson.annotations.JsonAdapter
import java.util.Date
import com.google.gson.annotations.SerializedName

data class PinDataResponse(
    @SerializedName("id") val id: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("location") val location: String,
    @SerializedName("user") val user: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("media_files") var media_files: List<String>,
    @SerializedName("info") val info: Any?,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("visibility") val visibility: String,
    @SerializedName("is_ads") val is_ads: Boolean?,
    @SerializedName("created_at") val created_at: Date,
    @SerializedName("updated_at") val updated_at: Date
)

