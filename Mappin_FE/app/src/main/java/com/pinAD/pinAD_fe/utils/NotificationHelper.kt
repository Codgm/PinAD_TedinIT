package com.pinAD.pinAD_fe.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {
    const val CHANNEL_ID_SERVICE = "location_service_channel"
    const val CHANNEL_ID_PINS = "pin_notification_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_SERVICE,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "위치 추적 서비스가 실행 중입니다"
                enableLights(true)
                enableVibration(true)
            }

            val pinsChannel = NotificationChannel(
                CHANNEL_ID_PINS,
                "Pin Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "새로운 핀을 발견했을 때 알림을 표시합니다"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(pinsChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}