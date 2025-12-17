package org.dicio.skill.reminder

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminder: Reminder): Long
    
    @Query("SELECT * FROM reminders WHERE active = 1 ORDER BY dueTimeMillis ASC")
    fun getAllActive(): Flow<List<Reminder>>
    
    @Update
    suspend fun update(reminder: Reminder)
    
    @Query("UPDATE reminders SET active = 0 WHERE id = :id")
    suspend fun deactivate(id: Int)
    
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: Int)
    
    @Query("SELECT * FROM reminders WHERE active = 1 AND dueTimeMillis <= :currentTime")
    suspend fun getDueReminders(currentTime: Long): List<Reminder>
}
