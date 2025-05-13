package com.pinAD.pinAD_fe.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pinAD.pinAD_fe.MainActivity
import com.pinAD.pinAD_fe.R

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM Service", "Message received: ${remoteMessage.notification?.body}")

        // Notification 데이터 추출
        val title = remoteMessage.notification?.title ?: "알림"
        val body = remoteMessage.notification?.body ?: ""
        val pinId = remoteMessage.data["pin_id"] ?: ""

        // 알림 생성
        sendNotification(title, body, pinId)
    }

    private fun sendNotification(title: String, body: String, pinId: String) {
        // MainActivity를 실행할 Intent 생성
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("pin_id", pinId)
            putExtra("should_focus_pin", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_PINS)
            .setSmallIcon(R.mipmap.pinad_logo_round)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        // 새로운 FCM 토큰이 생성될 때마다 서버에 전송
        // TODO: 서버에 새 토큰 전송 로직 구현
    }
}