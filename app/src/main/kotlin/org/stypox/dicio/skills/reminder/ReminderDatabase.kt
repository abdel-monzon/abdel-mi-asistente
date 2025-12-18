package org.stypox.dicio.skills.reminder

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ReminderEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    
    companion object {
        @Volatile
        private var Instance: ReminderDatabase? = null
        
        fun getDatabase(context: Context): ReminderDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    ReminderDatabase::class.java,
                    "reminder_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { Instance = it }
            }
        }
    }
}
