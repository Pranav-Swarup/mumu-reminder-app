package com.mumu.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mumu.app.data.model.Media
import com.mumu.app.data.model.Note
import com.mumu.app.data.model.Task

@Database(
    entities = [Task::class, Note::class, Media::class],
    version = 2,
    exportSchema = false
)
abstract class MuMuDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: MuMuDatabase? = null

        fun getDatabase(context: Context): MuMuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MuMuDatabase::class.java,
                    "mumu_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
