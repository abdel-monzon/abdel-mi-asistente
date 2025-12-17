package org.dicio.skill.reminder

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val dueTimeMillis: Long,  // Timestamp en milisegundos
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isDue(): Boolean {
        return dueTimeMillis <= System.currentTimeMillis()
    }
    
    fun getTimeRemaining(): String {
        val remaining = dueTimeMillis - System.currentTimeMillis()
        val hours = remaining / (1000 * 60 * 60)
        val minutes = (remaining % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            hours > 0 -> "en $hours horas y $minutes minutos"
            minutes > 0 -> "en $minutes minutos"
            else -> "ahora"
        }
    }
}
