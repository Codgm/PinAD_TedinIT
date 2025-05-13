package com.pinAD.pinAD_fe.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.android.gms.location.*
import com.pinAD.pinAD_fe.worker.LocationUpdateWorker
import com.pinAD.pinAD_fe.utils.NotificationHelper
import java.util.concurrent.TimeUnit

class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isServiceRunning = false

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_ID = 12345
        const val ACTION_START_LOCATION_SERVICE = "START_LOCATION_SERVICE"
        const val ACTION_STOP_LOCATION_SERVICE = "STOP_LOCATION_SERVICE"
        const val MIN_BACKOFF_MILLIS: Long = 1000
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Initializing LocationService")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // NotificationHelper를 통해 채널 생성
        NotificationHelper.createNotificationChannels(this)

        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Received action: ${intent?.action}")
        when (intent?.action) {
            ACTION_START_LOCATION_SERVICE -> startLocationUpdates()
            ACTION_STOP_LOCATION_SERVICE -> stopLocationService()
            else -> Log.w(TAG, "onStartCommand: Unknown action received")
        }
        return START_STICKY
    }

    private fun createNotification(): Notification {
        Log.d(TAG, "createNotification: Creating foreground service notification")
        val notificationIntent = packageManager
            .getLaunchIntentForPackage(packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID_SERVICE)
            .setContentTitle("위치 추적 중")
            .setContentText("백그라운드에서 위치를 추적하고 있습니다")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "onLocationResult: New location - Lat: ${location.latitude}, Lng: ${location.longitude}")
                    scheduleLocationUpdate(location.latitude.toFloat(), location.longitude.toFloat())
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (isServiceRunning) {
            Log.d(TAG, "startLocationUpdates: Service is already running")
            return
        }

        Log.d(TAG, "startLocationUpdates: Starting location updates")
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            TimeUnit.MINUTES.toMillis(5)
        ).build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )

            // 먼저 알림을 생성하고 startForeground 호출
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
            isServiceRunning = true
            Log.d(TAG, "startLocationUpdates: Location updates started successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "startLocationUpdates: Failed to start location updates", e)
            stopLocationService()
        }
    }

    private fun scheduleLocationUpdate(latitude: Float, longitude: Float) {
        val locationData = workDataOf(
            "latitude" to latitude,
            "longitude" to longitude
        )

        val updateRequest = OneTimeWorkRequestBuilder<LocationUpdateWorker>()
            .setInputData(locationData)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "location_update",
                ExistingWorkPolicy.REPLACE,
                updateRequest
            )
    }

    private fun stopLocationService() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
        isServiceRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

// Activity Extension
fun Activity.startLocationService() {
    val serviceIntent = Intent(this, LocationService::class.java).apply {
        action = LocationService.ACTION_START_LOCATION_SERVICE
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(serviceIntent)
    } else {
        startService(serviceIntent)
    }
}