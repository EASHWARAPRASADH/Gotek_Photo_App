package com.studentidphotocapture.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.studentidphotocapture.app.data.dao.UserDao
import com.studentidphotocapture.app.data.dao.StudentDao
import com.studentidphotocapture.app.data.dao.PhotoMetadataDao
import com.studentidphotocapture.app.data.model.User
import com.studentidphotocapture.app.data.model.Student
import com.studentidphotocapture.app.data.model.PhotoMetadata

@Database(
    entities = [User::class, Student::class, PhotoMetadata::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun photoMetadataDao(): PhotoMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_photo_v6_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
