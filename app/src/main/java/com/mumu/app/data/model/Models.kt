package com.mumu.app.data.model

import androidx.room.*
import java.util.UUID

enum class TaskType {
    RECURRING_ALARM,   // Type A
    URGENT_PUSH,       // Type B
    PASSIVE_TODO,      // Type C
    SEMI_PASSIVE       // Type D
}

enum class RepeatType {
    DAILY, WEEKLY, MONTHLY, NONE
}

enum class NotificationMode {
    ALERT,      // Normal chime + heads-up
    PASSIVE     // Silent, shows on unlock around the time
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val type: TaskType,
    val timestamp: Long = System.currentTimeMillis(),
    val dueTime: Long? = null,
    val completed: Boolean = false,
    val priority: Int = 0,
    val tags: String = "",
    val sortOrder: Int = 0,
    // Type A specifics
    val repeatType: RepeatType = RepeatType.NONE,
    val daysOfWeek: String = "",
    val monthDay: Int = 0,
    // Type B specifics
    val isPersistentNotification: Boolean = false,
    val repeatIntervalMinutes: Int = 0,
    // Type D specifics
    val isSilent: Boolean = false,
    val showOnUnlockOnly: Boolean = false,
    // Notification control
    val notificationMode: NotificationMode = NotificationMode.ALERT,
    val isAnonymous: Boolean = false, // "Something pending" instead of title
    // Snooze
    val snoozeDurationMinutes: Int = 5,
    val enableVibration: Boolean = true,
    val enableSound: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String = "",
    val isLocked: Boolean = false,
    val pinHash: String? = null,
    val backupQuestion: String? = null,
    val backupAnswerHash: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val color: Int = 0,
    val imagePaths: String = "" // comma-separated local file paths
)

@Entity(
    tableName = "media",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class Media(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val noteId: String,
    val filePath: String
)
