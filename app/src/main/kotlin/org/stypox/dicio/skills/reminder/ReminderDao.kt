package org.stypox.dicio.skills.reminder

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE is_cancelled = 0 AND is_completed = 0 ORDER BY timestamp ASC")
    fun getAllActiveReminders(): Flow<List<ReminderEntity>>
    
    @Query("SELECT * FROM reminders WHERE is_cancelled = 0 AND is_completed = 0 AND timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    fun getUpcomingReminders(start: Instant, end: Instant): Flow<List<ReminderEntity>>
    
    @Insert
    suspend fun insert(reminder: ReminderEntity): Long
    
    @Update
    suspend fun update(reminder: ReminderEntity)
    
    @Query("UPDATE reminders SET is_cancelled = 1 WHERE id = :id")
    suspend fun cancelById(id: Int)
    
    @Query("UPDATE reminders SET is_completed = 1 WHERE id = :id")
    suspend fun markAsCompleted(id: Int)
    
    @Query("DELETE FROM reminders WHERE is_cancelled = 1 OR is_completed = 1")
    suspend fun cleanupOldReminders()
    
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Int): ReminderEntity?
    
    @Query("SELECT COUNT(*) FROM reminders WHERE is_cancelled = 0 AND is_completed = 0")
    suspend fun getActiveCount(): Int
}
