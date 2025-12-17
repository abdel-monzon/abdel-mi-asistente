package org.stypox.dicio.skills.reminder

import org.dicio.skill.context.SkillContext
import org.stypox.dicio.R
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput

/**
 * Representa todos los tipos posibles de salida de la habilidad.
 */
sealed class ReminderOutput : HeadlineSpeechSkillOutput {
    /** Éxito al establecer un recordatorio */
    data class SetSuccess(
        val text: String,
        val timeExpression: String
    ) : ReminderOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String {
            return ctx.androidContext.getString(
                R.string.skill_reminder_set_success,
                text,
                timeExpression
            )
        }
    }
    
    /** Lista de recordatorios (puede estar vacía) */
    data class ListSuccess(
        val reminders: List<ReminderItem>
    ) : ReminderOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String {
            val context = ctx.androidContext
            
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
    
    /** Éxito al cancelar un recordatorio */
    data class CancelSuccess(
        val index: Int
    ) : ReminderOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String {
            return ctx.androidContext.getString(
                R.string.skill_reminder_cancel_success,
                index
            )
        }
    }
    
    /** Lista vacía (caso especial) */
    object ListEmpty : ReminderOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String {
            return ctx.androidContext.getString(R.string.skill_reminder_list_empty)
        }
    }
    
    /** Error genérico */
    data class Error(
        val message: String
    ) : ReminderOutput() {
        override fun getSpeechOutput(ctx: SkillContext): String {
            return message
        }
    }
}
