package com.mumu.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mumu.app.data.db.MuMuDatabase
import com.mumu.app.data.model.*
import com.mumu.app.data.repository.NoteRepository
import com.mumu.app.data.repository.TaskRepository
import com.mumu.app.notification.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MuMuDatabase.getDatabase(application)
    private val taskRepo = TaskRepository(db)
    private val noteRepo = NoteRepository(db)

    // Day boundaries for "today" queries
    private val todayStart: Long
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

    private val todayEnd: Long
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return cal.timeInMillis
        }

    val todayTasks: StateFlow<List<Task>> = taskRepo.getTodayTasks(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todos: StateFlow<List<Task>> = taskRepo.getTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<Task>> = taskRepo.getReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<Note>> = noteRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchResults: StateFlow<List<Note>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) noteRepo.getAll()
            else noteRepo.search(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun searchNotes(query: String) {
        _searchQuery.value = query
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            taskRepo.insert(task)
            if (task.type != TaskType.PASSIVE_TODO) {
                AlarmScheduler.schedule(getApplication(), task)
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepo.update(task)
            if (task.type != TaskType.PASSIVE_TODO) {
                AlarmScheduler.cancel(getApplication(), task)
                if (!task.completed) {
                    AlarmScheduler.schedule(getApplication(), task)
                }
            }
        }
    }

    fun toggleComplete(task: Task) {
        viewModelScope.launch {
            taskRepo.setCompleted(task.id, !task.completed)
            if (!task.completed) {
                // Being marked complete — cancel alarm
                AlarmScheduler.cancel(getApplication(), task)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            AlarmScheduler.cancel(getApplication(), task)
            taskRepo.delete(task)
        }
    }

    fun addNote(note: Note) {
        viewModelScope.launch { noteRepo.insert(note) }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteRepo.update(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { noteRepo.delete(note) }
    }

    fun reorderTodos(from: Int, to: Int) {
        viewModelScope.launch {
            val current = todos.value.toMutableList()
            if (from in current.indices && to in current.indices) {
                val moved = current.removeAt(from)
                current.add(to, moved)
                current.forEachIndexed { index, task ->
                    taskRepo.updateSortOrder(task.id, index)
                }
            }
        }
    }
}
