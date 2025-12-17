package org.dicio.skill.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        val id = inputData.getInt("reminder_id", 0)
        val text = inputData.getString("reminder_text") ?: "Recordatorio"
        
        showNotification(id, text)
        return Result.success()
    }
    
    private fun showNotification(id: Int, text: String) {
        val channelId = "reminder_channel"
        val notificationManager = 
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Crear canal (solo necesario en Android 8+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de recordatorios"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recordatorio")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(id, notification)
    }
}
