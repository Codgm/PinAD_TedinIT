package com.pinAD.pinAD_fe.Data.pin

import com.google.gson.annotations.SerializedName
import java.util.Date

data class FltPinData(
    @SerializedName("id") val id: Int,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("location") val location: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("user") val user: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("pin_type") val pin_type: Int,
//    @SerializedName("category") val category: String,
    @SerializedName("media") var media: String,
    @SerializedName("info") val info: Any?,
    @SerializedName("tags") val tags: List<FTag>,
    @SerializedName("visibility") val visibility: String,
    @SerializedName("is_ads") val is_ads: Boolean?,
    @SerializedName("created_at") val created_at: Date,
    @SerializedName("updated_at") val updated_at: Date
)
