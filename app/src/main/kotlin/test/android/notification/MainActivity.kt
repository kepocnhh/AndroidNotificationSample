package test.android.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlin.math.absoluteValue

internal class MainActivity : AppCompatActivity() {
    private fun showNotification() {
        showToast("show notification")
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "foo"
        notificationManager.checkNotificationChannel(
            channelId = channelId,
            lazyName = { channelId }
        )
        val notificationId = System.currentTimeMillis().toInt().absoluteValue
        val intent = Intent(this, TestActivity::class.java).also {
            it.putExtra("nId", notificationId.toString())
        }
        val actionAccept = buildNotificationAction(
            title = "open",
            pendingIntent(
                intent = intent,
                flags = PendingIntent.FLAG_UPDATE_CURRENT,
            )
        )
        val actionCancel = buildNotificationAction(
            title = "cancel",
            pendingIntent(
                intent = Intent(this, MainActivity::class.java).also {
                    it.action = "kill_notification"
                    it.putExtra("nId", notificationId.toString())
                },
                flags = PendingIntent.FLAG_UPDATE_CURRENT,
            )
        )
        notificationManager.notify(
            notificationId,
            buildNotification(
                channelId = channelId,
                title = "foo title",
                text = "bar text",
                pendingIntent = pendingIntent(intent = intent, flags = PendingIntent.FLAG_UPDATE_CURRENT),
                actions = listOf(
                    actionAccept,
                    actionCancel,
                ),
                color = Color.GREEN,
                autoCancel = true,
                ongoing = true,
                priority = NotificationCompat.PRIORITY_HIGH,
                defaults = NotificationCompat.DEFAULT_VIBRATE,
                smallIcon = R.drawable.ic_launcher_foreground,
            )
        )
    }

    private fun startActivity() {
        showToast("start activity")
        val intent = Intent(this, TestActivity::class.java).also {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun onClick() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (powerManager.isInteractive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = Manifest.permission.POST_NOTIFICATIONS
                val isGranted = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
                if (isGranted) {
                    showToast("permission is granted")
                    showNotification()
                } else {
                    showToast("permission is not granted")
                    startActivity()
                }
            } else {
                showToast("SDK: ${Build.VERSION.SDK_INT}")
                showNotification()
            }
        } else {
            showToast("no interactive")
            startActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        if (intent != null && intent.extras != null) {
            when (intent.action) {
                "kill_notification" -> {
                    val notificationId = intent.getStringExtra("nId")?.toIntOrNull() ?: return
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(notificationId)
                }
            }
        }
        val root = FrameLayout(this)
        val buttons = LinearLayout(this).also {
            it.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL,
            )
            it.orientation = LinearLayout.VERTICAL
            root.addView(it)
        }
        Button(this).also {
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            it.text = "click me"
            it.setOnClickListener {
                onClick()
            }
            buttons.addView(it)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Button(this).also {
                    it.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    it.text = "request permission"
                    it.setOnClickListener {
                        requestPermissions(arrayOf(permission), 0)
                    }
                    buttons.addView(it)
                }
            }
        }
        setContentView(root)
    }
}
