package org.stypox.dicio.skills.reminder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.Instant

@Entity(tableName = "reminders")
@TypeConverters(Converters::class)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "text")
    val text: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Instant,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Instant.now(),
    
    @ColumnInfo(name = "is_cancelled")
    val isCancelled: Boolean = false,
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false
) {
    val isActive: Boolean
        get() = !isCancelled && !isCompleted && timestamp.isAfter(Instant.now())
}
