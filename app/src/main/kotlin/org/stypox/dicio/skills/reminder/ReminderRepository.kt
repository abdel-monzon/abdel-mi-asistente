package org.stypox.dicio.skills.reminder

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderRepository(private val context: Context) {
    private val dao = ReminderDatabase.getDatabase(context).reminderDao()
    
    suspend fun addReminder(text: String, timestamp: Instant): Long {
        val reminder = ReminderEntity(
            text = text,
            timestamp = timestamp
        )
        return dao.insert(reminder)
    }
    
    fun getAllActiveReminders(): Flow<List<ReminderEntity>> {
        return dao.getAllActiveReminders()
    }
    
    suspend fun getReminderById(id: Int): ReminderEntity? {
        return dao.getById(id)
    }
    
    suspend fun cancelReminder(id: Int) {
        dao.cancelById(id)
    }
    
    suspend fun markAsCompleted(id: Int) {
        dao.markAsCompleted(id)
    }
    
    suspend fun getUpcomingReminders(hours: Int = 24): Flow<List<ReminderEntity>> {
        val now = Instant.now()
        val end = now.plus(Duration.ofHours(hours.toLong()))
        return dao.getUpcomingReminders(now, end)
    }
    
    suspend fun cleanupOldReminders() {
        dao.cleanupOldReminders()
    }
    
    suspend fun getActiveCount(): Int {
        return dao.getActiveCount()
    }
    
    fun formatTimeRemaining(timestamp: Instant): String {
        val now = Instant.now()
        val duration = Duration.between(now, timestamp)
        
        return when {
            duration.toDays() > 0 -> {
                val days = duration.toDays()
                val hours = duration.minusDays(days).toHours()
                "${days}d ${hours}h"
            }
            duration.toHours() > 0 -> {
                val hours = duration.toHours()
                val minutes = duration.minusHours(hours).toMinutes()
                "${hours}h ${minutes}m"
            }
            duration.toMinutes() > 0 -> "${duration.toMinutes()}m"
            else -> "ahora"
        }
    }
}
