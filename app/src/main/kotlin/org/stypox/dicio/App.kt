package org.stypox.dicio

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager

class App : Application(), Configuration.Provider {
    
    companion object {
        lateinit var context: Context
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        WorkManager.initialize(this, workManagerConfiguration)
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}
