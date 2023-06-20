package test.android.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat

internal fun NotificationManager.checkNotificationChannel(
    channelId: String,
    lazyName: () -> String,
    vibration: Boolean = false,
    importance: Int = NotificationManager.IMPORTANCE_HIGH,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    if (getNotificationChannel(channelId) != null) return
    val channel = NotificationChannel(
        channelId,
        lazyName(),
        importance,
    )
    channel.enableVibration(vibration)
    createNotificationChannel(channel)
}

internal fun buildNotificationAction(
    title: CharSequence,
    intent: PendingIntent,
): NotificationCompat.Action {
    return NotificationCompat.Action.Builder(
        0,
        title,
        intent,
    ).build()
}

internal fun Context.buildNotification(
    channelId: String,
    title: CharSequence,
    text: CharSequence,
    pendingIntent: PendingIntent,
    @DrawableRes smallIcon: Int,
    @ColorInt color: Int,
    actions: List<NotificationCompat.Action>,
    style: NotificationCompat.Style = NotificationCompat.DecoratedCustomViewStyle(),
    autoCancel: Boolean = true,
    ongoing: Boolean = false,
    defaults: Int? = null,
    priority: Int = NotificationCompat.PRIORITY_DEFAULT,
): Notification {
    return NotificationCompat.Builder(this, channelId)
        .setStyle(style)
        .setAutoCancel(autoCancel)
        .setOngoing(ongoing)
        .setSmallIcon(smallIcon)
        .setColor(color)
        .setContentIntent(pendingIntent)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(priority)
        .also { builder ->
            actions.forEach {
                builder.addAction(it)
            }
            if (defaults != null) {
                builder.setDefaults(defaults)
            }
        }
        .build()
}
