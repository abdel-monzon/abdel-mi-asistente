package org.stypox.dicio.skills.reminder

import org.dicio.skill.context.SkillContext
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput

class ReminderOutput(
    private val outputType: OutputType,
    private val data: ReminderData? = null
) : HeadlineSpeechSkillOutput {
    
    enum class OutputType {
        SET_SUCCESS,
        LIST_SUCCESS,
        CANCEL_SUCCESS,
        SET_ERROR_NO_TEXT,
        SET_ERROR_NO_TIME,
        SET_ERROR_PARSE_TIME,
        LIST_EMPTY,
        CANCEL_ERROR_NOT_FOUND,
        CANCEL_ERROR_NO_NUMBER
    }
    
    data class ReminderData(
        val text: String = "",
        val timeExpression: String = "",
        val index: Int = 0,
        val reminders: List<ReminderItem> = emptyList()
    )
    
    data class ReminderItem(
        val index: Int,
        val text: String,
        val timeRemaining: String,
        val dueTimeMillis: Long
    )
    
    override fun getSpeechOutput(ctx: SkillContext): String {
        val context = ctx.androidContext
        
        return when (outputType) {
            OutputType.SET_SUCCESS -> context.getString(
                R.string.skill_reminder_set_success,
                data?.text ?: "",
                data?.timeExpression ?: ""
            )
            
            OutputType.LIST_SUCCESS -> {
                if (data?.reminders.isNullOrEmpty()) {
                    context.getString(R.string.skill_reminder_list_empty)
                } else {
                    val header = context.getString(R.string.skill_reminder_list_header) + "\n"
                    val items = data!!.reminders.joinToString("\n") { reminder ->
                        context.getString(
                            R.string.skill_reminder_list_item,
                            reminder.index,
                            reminder.text,
                            reminder.timeRemaining
                        )
                    }
                    header + items
                }
            }
            
            OutputType.CANCEL_SUCCESS -> context.getString(
                R.string.skill_reminder_cancel_success,
                data?.index ?: 0
            )
            
            OutputType.SET_ERROR_NO_TEXT -> 
                context.getString(R.string.skill_reminder_set_error_no_text)
            
            OutputType.SET_ERROR_NO_TIME -> 
                context.getString(R.string.skill_reminder_set_error_no_time)
            
            OutputType.SET_ERROR_PARSE_TIME -> 
                context.getString(R.string.skill_reminder_set_error_parse_time)
            
            OutputType.LIST_EMPTY -> 
                context.getString(R.string.skill_reminder_list_empty)
            
            OutputType.CANCEL_ERROR_NOT_FOUND -> 
                context.getString(R.string.skill_reminder_cancel_error)
            
            OutputType.CANCEL_ERROR_NO_NUMBER -> 
                context.getString(R.string.skill_reminder_cancel_error_no_number)
        }
    }
}
