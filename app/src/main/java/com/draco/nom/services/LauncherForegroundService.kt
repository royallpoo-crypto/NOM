package com.draco.nom.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.ComponentName
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.draco.nom.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LauncherForegroundService : Service() {
    companion object {
        const val ACTION_START = "com.draco.nom.action.START_FOREGROUND"
        const val ACTION_STOP = "com.draco.nom.action.STOP_FOREGROUND"
        const val CHANNEL_ID = "launcher_foreground_channel"
        const val NOTIF_ID = 1001
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notif = buildNotification()
                startForeground(NOTIF_ID, notif)
                startRepeatingToast()
            }
            ACTION_STOP -> stopSelf()
            else -> {
                // If started without explicit action, ensure foreground
                val notif = buildNotification()
                startForeground(NOTIF_ID, notif)
                startRepeatingToast()
            }
        }

        return START_STICKY
    }

    private fun startRepeatingToast() {
        job?.cancel()
        job = serviceScope.launch {
            while (isActive) {
             //   Toast.makeText(applicationContext, "Welcome to Launcher", Toast.LENGTH_SHORT).show()
                launchActivity()
                delay(7000)
            }
        }
    }

  private fun launchActivity() {
    val intent = Intent().apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        component = ComponentName("etp.pushexample", "etp.pushexample.main")
    }
    Toast.makeText(applicationContext, "notification working", Toast.LENGTH_SHORT).show()
    applicationContext.startActivity(intent)
  }


    override fun onDestroy() {
        job?.cancel()
        serviceScope.coroutineContext[Job]?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Launcher Service"
            val descriptionText = "Shows launcher status"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NOM Launcher")
            .setContentText("Launcher running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)

        return builder.build()
    }
}
