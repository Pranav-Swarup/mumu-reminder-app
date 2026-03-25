package com.mumu.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mumu.app.data.model.NotificationMode
import com.mumu.app.data.model.RepeatType
import com.mumu.app.data.model.Task
import com.mumu.app.data.model.TaskType
import java.util.Calendar

object AlarmScheduler {

    fun schedule(context: Context, task: Task) {
        when (task.type) {
            TaskType.RECURRING_ALARM -> scheduleRecurring(context, task)
            TaskType.URGENT_PUSH -> scheduleExact(context, task)
            TaskType.SEMI_PASSIVE -> scheduleSemiPassive(context, task)
            else -> {}
        }
    }

    fun cancel(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = createAlarmIntent(context, task)
        alarmManager.cancel(intent)
        intent.cancel()
    }

    private fun scheduleExact(context: Context, task: Task) {
        val dueTime = task.dueTime ?: return
        if (dueTime <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = createAlarmIntent(context, task)

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueTime, intent)
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueTime, intent)
        }
    }

    private fun scheduleRecurring(context: Context, task: Task) {
        val dueTime = task.dueTime ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = createAlarmIntent(context, task)

        var triggerTime = dueTime
        val now = System.currentTimeMillis()
        if (triggerTime <= now) {
            triggerTime = calculateNextOccurrence(task, now)
        }
        if (triggerTime <= now) return

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, intent)
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, intent)
        }
    }

    private fun scheduleSemiPassive(context: Context, task: Task) {
        if (task.showOnUnlockOnly || task.notificationMode == NotificationMode.PASSIVE) {
            // For passive mode with a future date, we still schedule an alarm
            // but the receiver will show it silently
            if (task.dueTime != null && task.dueTime > System.currentTimeMillis()) {
                scheduleExact(context, task)
            }
            return
        }
        scheduleExact(context, task)
    }

    fun calculateNextOccurrence(task: Task, fromTime: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = task.dueTime ?: fromTime }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        return when (task.repeatType) {
            RepeatType.DAILY -> {
                val next = Calendar.getInstance().apply {
                    timeInMillis = fromTime
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (next.timeInMillis <= fromTime) next.add(Calendar.DAY_OF_YEAR, 1)
                next.timeInMillis
            }
            RepeatType.WEEKLY -> {
                val days = task.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
                if (days.isEmpty()) return fromTime
                val next = Calendar.getInstance().apply {
                    timeInMillis = fromTime
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                for (i in 0..7) {
                    val dayOfWeek = next.get(Calendar.DAY_OF_WEEK)
                    if (dayOfWeek in days && next.timeInMillis > fromTime) return next.timeInMillis
                    next.add(Calendar.DAY_OF_YEAR, 1)
                }
                next.timeInMillis
            }
            RepeatType.MONTHLY -> {
                val next = Calendar.getInstance().apply {
                    timeInMillis = fromTime
                    set(Calendar.DAY_OF_MONTH, task.monthDay.coerceIn(1, getActualMaximum(Calendar.DAY_OF_MONTH)))
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (next.timeInMillis <= fromTime) next.add(Calendar.MONTH, 1)
                next.timeInMillis
            }
            RepeatType.NONE -> fromTime
        }
    }

    private fun createAlarmIntent(context: Context, task: Task): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("task_type", task.type.name)
            putExtra("is_persistent", task.isPersistentNotification)
            putExtra("is_anonymous", task.isAnonymous)
            putExtra("notif_mode", task.notificationMode.name)
        }
        return PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
