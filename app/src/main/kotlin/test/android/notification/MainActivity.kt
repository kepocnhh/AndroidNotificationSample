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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

internal class MainActivity : AppCompatActivity() {
    private var textView: TextView? = null
    private var requestButton: TextView? = null

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
            pendingIntentService(
                intent = Intent(this, TestService::class.java).also {
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

    private fun onRequest() {
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

    private fun onClick() {
        lifecycleScope.launch {
            val textView = checkNotNull(textView)
            val requestButton = checkNotNull(requestButton)
            requestButton.isEnabled = false
            withContext(Dispatchers.Default) {
                val time = 5.seconds
                val start = System.currentTimeMillis().milliseconds
                while (true) {
                    val now = System.currentTimeMillis().milliseconds
                    val d = now - start
                    if (d > time) break
                    val result = (time - d).toDouble(DurationUnit.SECONDS)
                    textView.post {
                        textView.text = result.toString()
                    }
                    delay(250.milliseconds)
                }
            }
            onRequest()
            requestButton.isEnabled = true
            textView.text = ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this)
        val rows = LinearLayout(this).also {
            it.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL,
            )
            it.orientation = LinearLayout.VERTICAL
            root.addView(it)
        }
        TextView(this).also {
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            it.text = "..."
            it.setOnClickListener {
                onClick()
            }
            textView = it
            rows.addView(it)
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
            requestButton = it
            rows.addView(it)
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
                    rows.addView(it)
                }
            }
        }
        setContentView(root)
    }
}
