package com.pinAD.pinAD_fe.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.pinAD.pinAD_fe.worker.LocationUpdateWorker
import java.util.concurrent.TimeUnit

class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isServiceRunning = false

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_ID = 12345
        private const val CHANNEL_ID = "location_service_channel"
        const val ACTION_START_LOCATION_SERVICE = "START_LOCATION_SERVICE"
        const val ACTION_STOP_LOCATION_SERVICE = "STOP_LOCATION_SERVICE"
        const val MIN_BACKOFF_MILLIS: Long = 1000
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Initializing LocationService")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel: Creating notification channel")
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "위치 추적 서비스가 실행 중입니다"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        Log.d(TAG, "createNotificationChannel: Channel created successfully")
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

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("위치 추적 중")
            .setContentText("백그라운드에서 위치를 추적하고 있습니다")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun setupLocationCallback() {
        Log.d(TAG, "setupLocationCallback: Setting up location callback")
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(
                        TAG, "onLocationResult: New location received - " +
                            "Lat: ${location.latitude}, Lng: ${location.longitude}")
                    scheduleLocationUpdate(location.latitude.toFloat(), location.longitude.toFloat())
                } ?: Log.w(TAG, "onLocationResult: Location result was null")
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
            TimeUnit.MINUTES.toMillis(15)
        ).build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )

            startForeground(NOTIFICATION_ID, createNotification())
            isServiceRunning = true
            Log.d(TAG, "startLocationUpdates: Location updates started successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "startLocationUpdates: Failed to start location updates", e)
            stopLocationService()
        }
    }

    private fun scheduleLocationUpdate(latitude: Float, longitude: Float) {
        Log.d(
            TAG, "scheduleLocationUpdate: Scheduling location update work - " +
                "Lat: $latitude, Lng: $longitude")

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
        Log.d(TAG, "scheduleLocationUpdate: Work request enqueued successfully")
    }

    private fun stopLocationService() {
        Log.d(TAG, "stopLocationService: Stopping location service")
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
        isServiceRunning = false
        Log.d(TAG, "stopLocationService: Service stopped successfully")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
