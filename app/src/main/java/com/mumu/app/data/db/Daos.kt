package com.mumu.app.data.db

import androidx.room.*
import com.mumu.app.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY priority DESC, dueTime ASC")
    fun getAllActive(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE type = :type AND completed = 0 ORDER BY sortOrder ASC, dueTime ASC")
    fun getByType(type: TaskType): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE (type = 'URGENT_PUSH' OR (dueTime IS NOT NULL AND dueTime >= :dayStart AND dueTime <= :dayEnd)) AND completed = 0 ORDER BY priority DESC, dueTime ASC")
    fun getTodayTasks(dayStart: Long, dayEnd: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE type = 'PASSIVE_TODO' AND completed = 0 ORDER BY sortOrder ASC")
    fun getTodos(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE (type = 'RECURRING_ALARM' OR type = 'SEMI_PASSIVE') AND completed = 0 ORDER BY dueTime ASC")
    fun getReminders(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE type IN ('RECURRING_ALARM', 'URGENT_PUSH', 'SEMI_PASSIVE') AND completed = 0 AND dueTime IS NOT NULL")
    suspend fun getScheduledTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE tasks SET completed = :completed WHERE id = :id")
    suspend fun setCompleted(id: String, completed: Boolean)

    @Query("UPDATE tasks SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: String, order: Int)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun search(query: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: String): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}

@Dao
interface MediaDao {
    @Query("SELECT * FROM media WHERE noteId = :noteId")
    fun getForNote(noteId: String): Flow<List<Media>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: Media)

    @Delete
    suspend fun delete(media: Media)
}
