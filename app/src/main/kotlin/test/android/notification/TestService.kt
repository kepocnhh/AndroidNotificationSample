package test.android.notification

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

internal class TestService : Service() {
    private fun onStartCommand(intent: Intent) {
        when (intent.action) {
            "kill_notification" -> {
                val notificationId = intent.getStringExtra("nId")?.toIntOrNull() ?: return
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent != null) onStartCommand(intent)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
