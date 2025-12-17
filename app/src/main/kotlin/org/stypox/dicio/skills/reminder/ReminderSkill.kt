package org.dicio.skill.standard

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.dicio.numbers.parser.Parser
import org.dicio.numbers.util.Duration
import org.stypox.dicio.R
import org.stypox.dicio.Sections
import org.stypox.dicio.skill.reminder.ReminderManager
import java.time.LocalDateTime

class ReminderSkill : StandardSkill() {
    private lateinit var manager: ReminderManager
    private lateinit var parser: Parser
    private lateinit var context: Context
    
    override fun setInput(input: String, result: StandardResult) {
        context = getContext()
        manager = ReminderManager(context)
        
        val parserFormatter = Sections.getParserFormatter()
        parser = parserFormatter?.parser ?: run {
            setOutput(StandardOutput(context.getString(R.string.skill_reminder_set_error_parse_time)))
            return
        }
        
        when (result.sentenceId) {
            "set" -> handleSetReminder(input, result)
            "list" -> handleListReminders()
            "cancel" -> handleCancelReminder(result)
        }
    }
    
    private fun handleSetReminder(input: String, result: StandardResult) {
        val text = result.getCapturingGroup("text") ?: run {
            setOutput(StandardOutput(context.getString(R.string.skill_reminder_set_error_no_text)))
            return
        }
        
        val timeExpression = result.getCapturingGroup("time") ?: run {
            setOutput(StandardOutput(context.getString(R.string.skill_reminder_set_error_no_time)))
            return
        }
        
        runBlocking {
            try {
                val duration: Duration = parser.parseDuration(timeExpression)
                    .orElseThrow { IllegalArgumentException("No se pudo parsear el tiempo") }
                
                val triggerInMillis = (duration.toSeconds() * 1000).toLong()
                val id = manager.addReminder(text, triggerInMillis)
                
                val timeFormatted = formatDuration(duration)
                val successMessage = context.getString(
                    R.string.skill_reminder_set_success,
                    text,
                    timeFormatted
                )
                
                setOutput(StandardOutput(successMessage))
                
            } catch (e: Exception) {
                setOutput(StandardOutput(context.getString(R.string.skill_reminder_set_error_parse_time)))
            }
        }
    }
    
    private fun handleListReminders() {
        manager.getAllReminders { reminders ->
            if (reminders.isEmpty()) {
                setOutput(StandardOutput(context.getString(R.string.skill_reminder_list_empty)))
                return@getAllReminders
            }
            
            val header = context.getString(R.string.skill_reminder_list_header) + "\n"
            val items = reminders.mapIndexed { index, reminder ->
                context.getString(
                    R.string.skill_reminder_list_item,
                    index + 1,
                    reminder.text,
                    reminder.getTimeRemaining()
                )
            }.joinToString("\n")
            
            setOutput(StandardOutput(header + items))
        }
    }
    
    private fun handleCancelReminder(result: StandardResult) {
        val indexStr = result.getCapturingGroup("index") ?: run {
            setOutput(StandardOutput(context.getString(R.string.skill_reminder_cancel_error_no_number)))
            return
        }
        
        runBlocking {
            try {
                val index = indexStr.toInt() - 1
                manager.getAllReminders { reminders ->
                    if (index in reminders.indices) {
                        val reminder = reminders[index]
                        manager.cancelReminder(reminder.id)
                        val message = context.getString(
                            R.string.skill_reminder_cancel_success,
                            index + 1
                        )
                        setOutput(StandardOutput(message))
                    } else {
                        setOutput(StandardOutput(context.getString(R.string.skill_reminder_cancel_error)))
                    }
                }
            } catch (e: NumberFormatException) {
                setOutput(StandardOutput(context.getString(R.string.skill_reminder_cancel_error_no_number)))
            }
        }
    }
    
    private fun formatDuration(duration: Duration): String {
        return when {
            duration.toHours() >= 1 -> {
                val hours = duration.toHours().toInt()
                context.getString(R.string.skill_reminder_time_hours, hours)
            }
            duration.toMinutes() >= 1 -> {
                val minutes = duration.toMinutes().toInt()
                context.getString(R.string.skill_reminder_time_minutes, minutes)
            }
            else -> {
                val seconds = duration.toSeconds().toInt()
                if (seconds > 0) {
                    context.getString(R.string.skill_reminder_time_seconds, seconds)
                } else {
                    context.getString(R.string.skill_reminder_time_now)
                }
            }
        }
    }
}
