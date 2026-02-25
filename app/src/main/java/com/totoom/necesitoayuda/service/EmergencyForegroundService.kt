package com.totoom.necesitoayuda.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.totoom.necesitoayuda.R
import com.totoom.necesitoayuda.util.EmergencyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class EmergencyForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        val targetPhone = intent?.getStringExtra(EXTRA_TARGET_PHONE).orEmpty()
        val allPhones = intent?.getStringArrayListExtra(EXTRA_ALL_PHONES).orEmpty()

        serviceScope.launch {
            EmergencyManager(applicationContext).executeFullEmergencyFlow(targetPhone, allPhones)
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.emergency_flow_running))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.emergency_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_TARGET_PHONE = "extra_target_phone"
        const val EXTRA_ALL_PHONES = "extra_all_phones"

        private const val CHANNEL_ID = "emergency_flow_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
