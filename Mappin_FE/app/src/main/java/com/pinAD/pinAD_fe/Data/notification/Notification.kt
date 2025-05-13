package com.pinAD.pinAD_fe.Data.notification

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("notification_id") override val notification_id: Int,
    @SerializedName("title") override val title: String,
    @SerializedName("content") val content: Content,
    @SerializedName("status") override val status: String,
    @SerializedName("created_at") override val created_at: String
) : BaseNotification
