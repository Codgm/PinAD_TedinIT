package com.pinAD.pinAD_fe.Data.notification

interface BaseNotification {
    val notification_id: Int
    val title: String
    val status: String
    val created_at: String
}