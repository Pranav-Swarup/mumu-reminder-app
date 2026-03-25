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

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val type: TaskType,
    val timestamp: Long = System.currentTimeMillis(),
    val dueTime: Long? = null, // epoch millis for when reminder fires
    val completed: Boolean = false,
    val priority: Int = 0, // 0=normal, 1=high, 2=urgent
    val tags: String = "", // comma separated
    val sortOrder: Int = 0,
    // Type A specifics
    val repeatType: RepeatType = RepeatType.NONE,
    val daysOfWeek: String = "", // comma-separated 1-7 for weekly
    val monthDay: Int = 0, // for monthly
    // Type B specifics
    val isPersistentNotification: Boolean = false,
    val repeatIntervalMinutes: Int = 0, // 0 = no repeat
    // Type D specifics
    val isSilent: Boolean = false,
    val showOnUnlockOnly: Boolean = false,
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
    val pinHash: String? = null, // hashed PIN
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val color: Int = 0 // index into pastel palette
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
