package com.pinAD.pinAD_fe.worker

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pinAD.pinAD_fe.Data.location.LocationUpdateRequest
import com.pinAD.pinAD_fe.service.LocationService
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.network.RetrofitInstance
import com.pinAD.pinAD_fe.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "LocationUpdateWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "doWork: Starting location update work")
        try {
            val latitude = inputData.getFloat("latitude", 0f)
            val longitude = inputData.getFloat("longitude", 0f)
            Log.d(TAG, "doWork: Processing location - Lat: $latitude, Lng: $longitude")

            val token = RetrofitInstance.getAccessToken()
            if (token == null) {
                Log.e(TAG, "doWork: Token not found in RetrofitInstance")
                return@withContext Result.failure()
            }

            val locationRequest = LocationUpdateRequest(latitude, longitude)
            Log.d(TAG, "doWork: Sending location update to server")

            val response = RetrofitInstance.api.updateUserLocation(
                "Bearer $token",
                locationRequest
            )

            Log.d(TAG, "doWork: Server response received with ${response.entered_pins.size} new pins")

            // 핀 ID에 대한 정보를 비동기적으로 가져오고 알림 생성
            response.entered_pins.forEach { pinId ->
                val pinDataResponse = RetrofitInstance.api.getPinData(pinId)
                if (pinDataResponse.isSuccessful) {
                    val pinWrapper = pinDataResponse.body() // PinWrapper 타입인지 확인
                    if (pinWrapper != null) {
                        val pinData = pinWrapper.pin // FltPinData에 접근
                        Log.d(TAG, "doWork: Creating notification for pin ID: ${pinData.id} with title: ${pinData.title} and description: ${pinData.description}")
                        createPinNotification(pinData.title, pinData.description) // 핀 ID와 제목, 설명을 전달
                    } else {
                        Log.e(TAG, "doWork: PinWrapper data is null for pin ID: $pinId")
                    }
                } else {
                    Log.e(TAG, "doWork: Error fetching pin data for pin ID: $pinId, Response code: ${pinDataResponse.code()}")
                }
            }

            Log.d(TAG, "doWork: Location update completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork: Error updating location", e)
            if (runAttemptCount < 3) {
                Log.d(TAG, "doWork: Retrying... (Attempt ${runAttemptCount + 1}/3)")
                Result.retry()
            } else {
                Log.e(TAG, "doWork: Max retry attempts reached, marking as failed")
                Result.failure()
            }
        }
    }

    private fun createPinNotification(title: String, description: String) {
        try {
            // 알림 권한 체크
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "createPinNotification: Notification permission not granted")
                return
            }

            // NotificationManager 서비스 가져오기
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_PINS)
                .setContentTitle("새로운 $title 핀 발견")
                .setContentText(description)
                .setSmallIcon(R.drawable.ic_event)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            // try-catch로 SecurityException 처리
            try {
                notificationManager.notify(title.hashCode(), notification)
                Log.d(TAG, "createPinNotification: Notification created successfully for pin: $title")
            } catch (se: SecurityException) {
                Log.e(TAG, "createPinNotification: Failed to show notification due to permission issue", se)
            }
        } catch (e: Exception) {
            Log.e(TAG, "createPinNotification: Error creating notification", e)
        }
    }
}
