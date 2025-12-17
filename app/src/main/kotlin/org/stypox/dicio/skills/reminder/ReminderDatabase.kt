package org.dicio.skill.reminder

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [Reminder::class],
    version = 1,
    exportSchema = false
)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    
    companion object {
        @Volatile
        private var INSTANCE: ReminderDatabase? = null
        
        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminders.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
