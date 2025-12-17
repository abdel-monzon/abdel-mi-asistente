package org.stypox.dicio.skills.reminder

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.skill.Specificity
import org.dicio.skill.standard.StandardScore
import org.stypox.dicio.sentences.Sentences

class ReminderSkill : Skill<ReminderInput?>(ReminderInfo, Specificity.HIGH) {
    
    override fun score(ctx: SkillContext, input: String): Pair<StandardScore, ReminderInput?> {
        // 1. Obtener el reconocedor para el idioma actual
        val recognizerData = Sentences.Reminder[ctx.sentencesLanguage]
        
        return if (recognizerData != null) {
            // 2. Usar el reconocedor para evaluar la entrada
            val result = recognizerData.score(input)
            
            // 3. Extraer los datos de la frase reconocida
            val reminderInput = ReminderInput.fromStandardResult(result)
            
            // 4. Devolver la puntuación junto con los datos extraídos
            Pair(result.score, reminderInput)
        } else {
            // Si no hay reconocedor disponible
            Pair(StandardScore.EMPTY, null)
        }
    }
    
    override suspend fun generateOutput(ctx: SkillContext, inputData: ReminderInput?): SkillOutput {
        // Si no se pudo interpretar la entrada
        if (inputData == null) {
            return ReminderOutput.Error(
                message = ctx.androidContext.getString(
                    org.stypox.dicio.R.string.skill_reminder_set_error_parse_time
                )
            )
        }
        
        // Procesar según el tipo de comando
        return when (inputData) {
            is ReminderInput.Set -> handleSetReminder(ctx, inputData)
            is ReminderInput.List -> handleListReminders(ctx)
            is ReminderInput.Cancel -> handleCancelReminder(ctx, inputData)
        }
    }
    
    private fun handleSetReminder(ctx: SkillContext, input: ReminderInput.Set): ReminderOutput {
        val context = ctx.androidContext
        
        // Validaciones básicas
        if (input.text.isBlank()) {
            return ReminderOutput.Error(
                message = context.getString(org.stypox.dicio.R.string.skill_reminder_set_error_no_text)
            )
        }
        
        if (input.timeExpression.isBlank()) {
            return ReminderOutput.Error(
                message = context.getString(org.stypox.dicio.R.string.skill_reminder_set_error_no_time)
            )
        }
        
        // Por ahora, solo confirmamos que recibimos el comando
        // En la Fase 2 parsearemos el tiempo y guardaremos el recordatorio
        return ReminderOutput.SetSuccess(
            text = input.text,
            timeExpression = input.timeExpression
        )
    }
    
    private fun handleListReminders(ctx: SkillContext): ReminderOutput {
        // Por ahora, lista vacía
        // En la Fase 2 recuperaremos recordatorios reales de una base de datos
        val reminders = emptyList<ReminderItem>()
        
        return if (reminders.isEmpty()) {
            ReminderOutput.ListEmpty
        } else {
            ReminderOutput.ListSuccess(reminders)
        }
    }
    
    private fun handleCancelReminder(ctx: SkillContext, input: ReminderInput.Cancel): ReminderOutput {
        // Por ahora, solo confirmamos
        // En la Fase 2 buscaremos y eliminaremos el recordatorio real
        return ReminderOutput.CancelSuccess(
            index = input.index
        )
    }
}

// Datos de ejemplo para listar (se usará en la Fase 2)
data class ReminderItem(
    val id: Int,
    val text: String,
    val timeRemaining: String
)
