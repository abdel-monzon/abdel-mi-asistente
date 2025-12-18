package org.stypox.dicio

import android.Manifest
import android.app.Application
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import org.stypox.dicio.skills.reminder.ReminderWorker
import org.stypox.dicio.util.checkPermissions
import javax.inject.Inject

// IMPORTANT NOTE: beware of this nasty bug related to allowBackup=true
// https://medium.com/p/924c91bafcac
@HiltAndroidApp
class App : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        
        // ✅ INICIALIZAR THREETENABP PARA FECHAS/HORAS
        // Esto es esencial para el parser de tiempo de los recordatorios
        AndroidThreeTen.init(this)
        
        // ✅ INICIALIZAR CANALES DE NOTIFICACIÓN
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkPermissions(this, Manifest.permission.POST_NOTIFICATIONS)
        ) {
            initNotificationChannels()
        }
        
        // ✅ LOG INICIAL PARA VERIFICAR
        android.util.Log.d("DicioApp", "Aplicación inicializada - Recordatorios activos")
    }

    private fun initNotificationChannels() {
        val notificationManager = NotificationManagerCompat.from(this)
        
        // Canal existente para reportes de error
        val errorChannel = NotificationChannelCompat.Builder(
            getString(R.string.error_report_channel_id),
            NotificationManagerCompat.IMPORTANCE_LOW
        )
            .setName(getString(R.string.error_report_channel_name))
            .setDescription(getString(R.string.error_report_channel_description))
            .build()
        
        // ✅ NUEVO: Canal para recordatorios
        val reminderChannel = NotificationChannelCompat.Builder(
            ReminderWorker.CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName(getString(R.string.reminder_channel_name))
            .setDescription(getString(R.string.reminder_channel_description))
            .setVibrationEnabled(true)
            .setVibrationPattern(longArrayOf(0, 500, 250, 500))
            .setLightsEnabled(true)
            .build()
        
        notificationManager.createNotificationChannelsCompat(
            listOf(errorChannel, reminderChannel)
        )
        
        android.util.Log.d("DicioApp", "Canales de notificación creados")
    }

    // ✅ NUEVO: Configuración para WorkManager con Hilt
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }
}
