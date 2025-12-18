package org.stypox.dicio.skills.reminder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.stypox.dicio.MainActivity
import org.stypox.dicio.R
import java.time.Instant

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val NOTIFICATION_ID = 1001
    }
    
    override suspend fun doWork(): Result {
        return try {
            val repository = ReminderRepository(context)
            
            // Buscar recordatorios que deben activarse ahora (dentro de 1 minuto)
            val now = Instant.now()
            val oneMinuteFromNow = now.plusSeconds(60)
            
            val upcoming = repository.getUpcomingReminders(1) // Próximas 24 horas
            upcoming.collect { reminders ->
                reminders.forEach { reminder ->
                    // Si el recordatorio está dentro del próximo minuto, mostrar notificación
                    if (reminder.timestamp.isBefore(oneMinuteFromNow) && 
                        reminder.timestamp.isAfter(now.minusSeconds(60))) {
                        
                        showNotification(reminder)
                        repository.markAsCompleted(reminder.id)
                    }
                }
            }
            
            // Limpiar recordatorios antiguos
            repository.cleanupOldReminders()
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun showNotification(reminder: ReminderEntity) {
        // Intent para abrir la app al hacer clic
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Crear notificación
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(context.getString(R.string.notification_reminder_title))
            .setContentText(reminder.text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Mostrar notificación
        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID + reminder.id,
            notification
        )
    }
}
