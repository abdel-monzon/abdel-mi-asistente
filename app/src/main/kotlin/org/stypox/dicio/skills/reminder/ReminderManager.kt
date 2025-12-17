package org.dicio.skill.reminder

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context) {
    private val database = ReminderDatabase.getDatabase(context)
    private val dao = database.reminderDao()
    private val scope = CoroutineScope(Dispatchers.IO)
    
    suspend fun addReminder(text: String, triggerInMillis: Long): Long {
        val reminder = Reminder(
            text = text,
            dueTimeMillis = System.currentTimeMillis() + triggerInMillis
        )
        
        val id = dao.insert(reminder)
        scheduleNotification(reminder.copy(id = id.toInt()))
        
        return id
    }
    
    fun getAllReminders(callback: (List<Reminder>) -> Unit) {
        scope.launch {
            dao.getAllActive().collect { reminders ->
                callback(reminders)
            }
        }
    }
    
    suspend fun cancelReminder(id: Int) {
        dao.deactivate(id)
        cancelNotification(id)
    }
    
    private fun scheduleNotification(reminder: Reminder) {
        val delay = reminder.dueTimeMillis - System.currentTimeMillis()
        
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "reminder_id" to reminder.id,
                    "reminder_text" to reminder.text
                )
            )
            .build()
        
        WorkManager.getInstance(context).enqueue(reminder.id.toString(), workRequest)
    }
    
    private fun cancelNotification(id: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(id.toString())
    }
}
