package com.mumu.app.data.repository

import com.mumu.app.data.db.MuMuDatabase
import com.mumu.app.data.model.*
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val db: MuMuDatabase) {
    private val dao = db.taskDao()

    fun getAllActive(): Flow<List<Task>> = dao.getAllActive()
    fun getByType(type: TaskType): Flow<List<Task>> = dao.getByType(type)
    fun getTodayTasks(dayStart: Long, dayEnd: Long): Flow<List<Task>> = dao.getTodayTasks(dayStart, dayEnd)
    fun getTodos(): Flow<List<Task>> = dao.getTodos()
    fun getReminders(): Flow<List<Task>> = dao.getReminders()
    suspend fun getScheduledTasks(): List<Task> = dao.getScheduledTasks()
    suspend fun getById(id: String): Task? = dao.getById(id)
    suspend fun insert(task: Task) = dao.insert(task)
    suspend fun update(task: Task) = dao.update(task)
    suspend fun delete(task: Task) = dao.delete(task)
    suspend fun deleteById(id: String) = dao.deleteById(id)
    suspend fun setCompleted(id: String, completed: Boolean) = dao.setCompleted(id, completed)
    suspend fun updateSortOrder(id: String, order: Int) = dao.updateSortOrder(id, order)
}

class NoteRepository(private val db: MuMuDatabase) {
    private val dao = db.noteDao()

    fun getAll(): Flow<List<Note>> = dao.getAll()
    fun search(query: String): Flow<List<Note>> = dao.search(query)
    suspend fun getById(id: String): Note? = dao.getById(id)
    suspend fun insert(note: Note) = dao.insert(note)
    suspend fun update(note: Note) = dao.update(note)
    suspend fun delete(note: Note) = dao.delete(note)
}
