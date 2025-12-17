package org.stypox.dicio.skills.reminder

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.sentences.Sentences.Reminder
import org.stypox.dicio.util.getString

class ReminderSkill(
    correspondingSkillInfo: ReminderInfo,
    data: StandardRecognizerData<Reminder>
) : StandardRecognizerSkill<Reminder>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: Reminder): SkillOutput {
        return when (inputData) {
            is Reminder.Set -> handleSetReminder(ctx, inputData)
            is Reminder.List -> handleListReminders(ctx)
            is Reminder.Cancel -> handleCancelReminder(ctx, inputData)
        }
    }
    
    private fun handleSetReminder(ctx: SkillContext, input: Reminder.Set): SkillOutput {
        val context = ctx.android
        
        // Validaciones básicas
        if (input.text.isNullOrBlank()) {
            return ReminderOutput.Error(
                message = context.getString(org.stypox.dicio.R.string.skill_reminder_set_error_no_text)
            )
        }
        
        if (input.time.isNullOrBlank()) {
            return ReminderOutput.Error(
                message = context.getString(org.stypox.dicio.R.string.skill_reminder_set_error_no_time)
            )
        }
        
        // Por ahora, solo confirmamos (en Fase 2 parsearemos el tiempo)
        return ReminderOutput.SetSuccess(
            text = input.text!!,
            timeExpression = input.time!!
        )
    }
    
    private fun handleListReminders(ctx: SkillContext): SkillOutput {
        // Por ahora, lista vacía
        val reminders = emptyList<ReminderOutput.ReminderItem>()
        
        return if (reminders.isEmpty()) {
            ReminderOutput.ListEmpty
        } else {
            ReminderOutput.ListSuccess(reminders)
        }
    }
    
    private fun handleCancelReminder(ctx: SkillContext, input: Reminder.Cancel): SkillOutput {
        val index = input.index?.toIntOrNull()
        
        if (index == null) {
            return ReminderOutput.Error(
                message = ctx.android.getString(
                    org.stypox.dicio.R.string.skill_reminder_cancel_error_no_number
                )
            )
        }
        
        // Por ahora, solo confirmamos (en Fase 2 buscaremos y eliminaremos)
        return ReminderOutput.CancelSuccess(
            index = index
        )
    }
}
