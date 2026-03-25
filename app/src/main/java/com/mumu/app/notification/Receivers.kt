package com.mumu.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mumu.app.data.db.MuMuDatabase
import com.mumu.app.data.model.TaskType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id") ?: return
        val title = intent.getStringExtra("task_title") ?: "Reminder"
        val type = intent.getStringExtra("task_type") ?: ""
        val isPersistent = intent.getBooleanExtra("is_persistent", false)
        val notifId = abs(taskId.hashCode()) % 100000

        when (type) {
            TaskType.RECURRING_ALARM.name -> {
                NotificationHelper.showAlarmNotification(context, taskId, title, notifId)
                // Reschedule for next occurrence
                rescheduleRecurring(context, taskId)
            }
            TaskType.URGENT_PUSH.name -> {
                NotificationHelper.showUrgentNotification(context, taskId, title, isPersistent, notifId)
            }
            TaskType.SEMI_PASSIVE.name -> {
                NotificationHelper.showSilentNotification(context, taskId, title, notifId)
            }
        }
    }

    private fun rescheduleRecurring(context: Context, taskId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = MuMuDatabase.getDatabase(context)
            val task = db.taskDao().getById(taskId) ?: return@launch
            if (task.repeatType != com.mumu.app.data.model.RepeatType.NONE && !task.completed) {
                val nextTime = AlarmScheduler.calculateNextOccurrence(task, System.currentTimeMillis())
                val updated = task.copy(dueTime = nextTime)
                db.taskDao().update(updated)
                AlarmScheduler.schedule(context, updated)
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        NotificationHelper.createChannels(context)

        CoroutineScope(Dispatchers.IO).launch {
            val db = MuMuDatabase.getDatabase(context)
            val tasks = db.taskDao().getScheduledTasks()
            tasks.forEach { task ->
                AlarmScheduler.schedule(context, task)
            }
        }
    }
}

class ScreenUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_USER_PRESENT) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = MuMuDatabase.getDatabase(context)
            val tasks = db.taskDao().getScheduledTasks()
            val now = System.currentTimeMillis()
            tasks.filter { task ->
                task.type == TaskType.SEMI_PASSIVE &&
                task.showOnUnlockOnly &&
                !task.completed &&
                (task.dueTime == null || task.dueTime <= now)
            }.forEach { task ->
                val notifId = abs(task.id.hashCode()) % 100000
                NotificationHelper.showSilentNotification(context, task.id, task.title, notifId)
            }
        }
    }
}

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id") ?: return
        val notifId = intent.getIntExtra("notif_id", 0)

        when (intent.action) {
            "MARK_DONE" -> {
                NotificationHelper.cancel(context, notifId)
                CoroutineScope(Dispatchers.IO).launch {
                    val db = MuMuDatabase.getDatabase(context)
                    db.taskDao().setCompleted(taskId, true)
                }
            }
            "SNOOZE" -> {
                NotificationHelper.cancel(context, notifId)
                CoroutineScope(Dispatchers.IO).launch {
                    val db = MuMuDatabase.getDatabase(context)
                    val task = db.taskDao().getById(taskId) ?: return@launch
                    val snoozeMs = task.snoozeDurationMinutes * 60 * 1000L
                    val snoozed = task.copy(dueTime = System.currentTimeMillis() + snoozeMs)
                    db.taskDao().update(snoozed)
                    AlarmScheduler.schedule(context, snoozed)
                }
            }
        }
    }
}
