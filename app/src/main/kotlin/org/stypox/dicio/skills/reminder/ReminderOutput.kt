package org.stypox.dicio.skills.reminder

import org.dicio.skill.context.SkillContext
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput

sealed class ReminderOutput : HeadlineSpeechSkillOutput {
    
    data class SetSuccess(
        val text: String,
        val timeExpression: String
    ) : ReminderOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String {
            return ctx.android.getString(
                R.string.skill_reminder_set_success,
                text,
                timeExpression
            )
        }
    }
    
    data class ListSuccess(
        val reminders: List<ReminderItem>
    ) : ReminderOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String {
            val context = ctx.android
            
            if (reminders.isEmpty()) {
                return context.getString(R.string.skill_reminder_list_empty)
            }
            
            val header = context.getString(R.string.skill_reminder_list_header) + "\n"
            val items = reminders.joinToString("\n") { reminder ->
                context.getString(
                    R.string.skill_reminder_list_item,
                    reminder.id,
                    reminder.text,
                    reminder.timeRemaining
                )
            }
            
            return header + items
        }
    }
    
    data class CancelSuccess(
        val index: Int
    ) : ReminderOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String {
            return ctx.android.getString(
                R.string.skill_reminder_cancel_success,
                index
            )
        }
    }
    
    object ListEmpty : ReminderOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String {
            return ctx.android.getString(R.string.skill_reminder_list_empty)
        }
    }
    
    data class Error(
        val message: String
    ) : ReminderOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String {
            return message
        }
    }
    
    data class ReminderItem(
        val id: Int,
        val text: String,
        val timeRemaining: String
    )
}
