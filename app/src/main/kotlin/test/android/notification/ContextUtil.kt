package test.android.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast

internal fun Context.showToast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

internal fun Context.pendingIntent(
    requestCode: Int = 0,
    intent: Intent,
    flags: Int,
): PendingIntent {
    return PendingIntent.getActivity(this, requestCode, intent, flags or PendingIntent.FLAG_IMMUTABLE)
}
