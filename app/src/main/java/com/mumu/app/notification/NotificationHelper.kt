package com.mumu.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mumu.app.MainActivity
import com.mumu.app.R

object NotificationHelper {
    const val CHANNEL_ALARM = "mumu_alarm"
    const val CHANNEL_URGENT = "mumu_urgent"
    const val CHANNEL_SILENT = "mumu_silent"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alarm = NotificationChannel(
            CHANNEL_ALARM, "Recurring Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Habit and routine reminders"
            enableVibration(true)
        }

        val urgent = NotificationChannel(
            CHANNEL_URGENT, "Urgent Tasks",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Must-do-today tasks"
            enableVibration(true)
            setBypassDnd(true)
        }

        val silent = NotificationChannel(
            CHANNEL_SILENT, "Gentle Reminders",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Soft nudge reminders"
            enableVibration(false)
            setSound(null, null)
        }

        manager.createNotificationChannels(listOf(alarm, urgent, silent))
    }

    fun showAlarmNotification(
        context: Context, taskId: String, title: String,
        notifId: Int, isAnonymous: Boolean = false
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val displayTitle = if (isAnonymous) "Hey, something's pending" else title
        val displayBody = if (isAnonymous) "Open the app to see what it is" else "Time for your reminder"

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context, notifId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, ActionReceiver::class.java).apply {
            action = "MARK_DONE"
            putExtra("task_id", taskId)
            putExtra("notif_id", notifId)
        }
        val donePending = PendingIntent.getBroadcast(
            context, notifId + 10000, doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ActionReceiver::class.java).apply {
            action = "SNOOZE"
            putExtra("task_id", taskId)
            putExtra("notif_id", notifId)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context, notifId + 20000, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(displayTitle)
            .setContentText(displayBody)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPending)
            .setAutoCancel(true)

        if (!isAnonymous) {
            builder.addAction(R.drawable.ic_notification, "Done", donePending)
            builder.addAction(R.drawable.ic_notification, "Snooze", snoozePending)
        }

        manager.notify(notifId, builder.build())
    }

    fun showUrgentNotification(
        context: Context, taskId: String, title: String,
        persistent: Boolean, notifId: Int, isAnonymous: Boolean = false
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val displayTitle = if (isAnonymous) "Something urgent pending" else title
        val displayBody = if (isAnonymous) "Open the app to check" else "Urgent — must do today!"

        val doneIntent = Intent(context, ActionReceiver::class.java).apply {
            action = "MARK_DONE"
            putExtra("task_id", taskId)
            putExtra("notif_id", notifId)
        }
        val donePending = PendingIntent.getBroadcast(
            context, notifId + 10000, doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_URGENT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(displayTitle)
            .setContentText(displayBody)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(persistent)

        if (!isAnonymous) {
            builder.addAction(R.drawable.ic_notification, "Mark Done", donePending)
        }

        manager.notify(notifId, builder.build())
    }

    fun showSilentNotification(
        context: Context, taskId: String, title: String,
        notifId: Int, isAnonymous: Boolean = false
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val displayTitle = if (isAnonymous) "A gentle nudge" else title
        val displayBody = if (isAnonymous) "Open the app to see" else "A gentle reminder"

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context, notifId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_SILENT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(displayTitle)
            .setContentText(displayBody)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .build()

        manager.notify(notifId, notif)
    }

    fun cancel(context: Context, notifId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notifId)
    }
}
