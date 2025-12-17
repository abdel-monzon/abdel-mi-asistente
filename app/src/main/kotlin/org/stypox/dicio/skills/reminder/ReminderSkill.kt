package org.stypox.dicio.skills.reminder

import kotlinx.coroutines.runBlocking
import org.dicio.numbers.parser.Parser
import org.dicio.numbers.util.Duration
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.skill.Specificity
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardResult
import org.dicio.skill.standard.StandardScore
import org.stypox.dicio.Sections
import org.stypox.dicio.sentences.Sentences
import java.util.concurrent.atomic.AtomicInteger

class ReminderSkill : Skill<ReminderInputData>(ReminderInfo, Specificity.HIGH) {
    
    // Datos en memoria (para simplificar - en producción usar Room)
    companion object {
        private val reminders = mutableListOf<ReminderData>()
        private val idCounter = AtomicInteger(1)
        
        data class ReminderData(
            val id: Int,
            val text: String,
            val dueTimeMillis: Long,
            val createdAt: Long = System.currentTimeMillis()
        )
    }
    
    data class ReminderInputData(
        val sentenceType: ReminderSentenceType,
        val text: String? = null,
        val timeExpression: String? = null,
        val index: Int? = null
    )
    
    enum class ReminderSentenceType {
        SET, LIST, CANCEL
    }
    
    override fun score(ctx: SkillContext, input: String): Pair<StandardScore, ReminderInputData> {
        // Usar el reconocedor estándar de frases
        val recognizerData = Sentences.Reminder[ctx.sentencesLanguage]
        
        return if (recognizerData != null) {
            val result = recognizerData.score(input)
            
            if (result.sentenceId != null) {
                // Extraer los datos según el tipo de frase
                val inputData = when (result.sentenceId) {
                    "set" -> {
                        val text = result.getCapturingGroup("text")
                        val time = result.getCapturingGroup("time")
                        ReminderInputData(
                            sentenceType = ReminderSentenceType.SET,
                            text = text,
                            timeExpression = time
                        )
                    }
                    "list" -> {
                        ReminderInputData(sentenceType = ReminderSentenceType.LIST)
                    }
                    "cancel" -> {
                        val indexStr = result.getCapturingGroup("index")
                        val index = indexStr?.toIntOrNull()
                        ReminderInputData(
                            sentenceType = ReminderSentenceType.CANCEL,
                            index = index
                        )
                    }
                    else -> ReminderInputData(sentenceType = ReminderSentenceType.LIST)
                }
                
                Pair(result.score, inputData)
            } else {
                Pair(StandardScore.EMPTY, ReminderInputData(sentenceType = ReminderSentenceType.LIST))
            }
        } else {
            Pair(StandardScore.EMPTY, ReminderInputData(sentenceType = ReminderSentenceType.LIST))
        }
    }
    
    override suspend fun generateOutput(ctx: SkillContext, inputData: ReminderInputData): SkillOutput {
        return when (inputData.sentenceType) {
            ReminderSentenceType.SET -> handleSetReminder(ctx, inputData)
            ReminderSentenceType.LIST -> handleListReminders(ctx)
            ReminderSentenceType.CANCEL -> handleCancelReminder(ctx, inputData)
        }
    }
    
    private fun handleSetReminder(ctx: SkillContext, inputData: ReminderInputData): SkillOutput {
        val text = inputData.text
        val timeExpression = inputData.timeExpression
        
        if (text.isNullOrEmpty()) {
            return ReminderOutput(
                ReminderOutput.OutputType.SET_ERROR_NO_TEXT
            )
        }
        
        if (timeExpression.isNullOrEmpty()) {
            return ReminderOutput(
                ReminderOutput.OutputType.SET_ERROR_NO_TIME
            )
        }
        
        return try {
            // Obtener el parser para el idioma actual
            val parser = Sections.getParserFormatter()?.parser
                ?: return ReminderOutput(
                    ReminderOutput.OutputType.SET_ERROR_PARSE_TIME
                )
            
            // Parsear la duración usando dicio-numbers
            val duration: Duration = runBlocking {
                parser.parseDuration(timeExpression).get()
            }
            
            // Calcular tiempo de activación
            val triggerInMillis = (duration.toSeconds() * 1000).toLong()
            val dueTimeMillis = System.currentTimeMillis() + triggerInMillis
            
            // Crear y guardar el recordatorio
            val reminder = ReminderData(
                id = idCounter.getAndIncrement(),
                text = text,
                dueTimeMillis = dueTimeMillis
            )
            reminders.add(reminder)
            
            // Programar notificación (esto es un esqueleto - implementar después)
            // scheduleNotification(reminder)
            
            ReminderOutput(
                outputType = ReminderOutput.OutputType.SET_SUCCESS,
                data = ReminderOutput.ReminderData(
                    text = text,
                    timeExpression = formatDuration(duration)
                )
            )
            
        } catch (e: Exception) {
            ReminderOutput(
                ReminderOutput.OutputType.SET_ERROR_PARSE_TIME
            )
        }
    }
    
    private fun handleListReminders(ctx: SkillContext): SkillOutput {
        val now = System.currentTimeMillis()
        val activeReminders = reminders
            .filter { it.dueTimeMillis > now }
            .sortedBy { it.dueTimeMillis }
        
        if (activeReminders.isEmpty()) {
            return ReminderOutput(
                ReminderOutput.OutputType.LIST_EMPTY
            )
        }
        
        val reminderItems = activeReminders.mapIndexed { index, reminder ->
            ReminderOutput.ReminderItem(
                index = index + 1,
                text = reminder.text,
                timeRemaining = calculateTimeRemaining(reminder.dueTimeMillis),
                dueTimeMillis = reminder.dueTimeMillis
            )
        }
        
        return ReminderOutput(
            outputType = ReminderOutput.OutputType.LIST_SUCCESS,
            data = ReminderOutput.ReminderData(
                reminders = reminderItems
            )
        )
    }
    
    private fun handleCancelReminder(ctx: SkillContext, inputData: ReminderInputData): SkillOutput {
        val index = inputData.index
        
        if (index == null) {
            return ReminderOutput(
                ReminderOutput.OutputType.CANCEL_ERROR_NO_NUMBER
            )
        }
        
        // Buscar por índice (no por ID)
        if (index <= 0 || index > reminders.size) {
            return ReminderOutput(
                ReminderOutput.OutputType.CANCEL_ERROR_NOT_FOUND
            )
        }
        
        val reminderToRemove = reminders[index - 1]
        reminders.removeAt(index - 1)
        
        // Cancelar notificación programada
        // cancelNotification(reminderToRemove.id)
        
        return ReminderOutput(
            outputType = ReminderOutput.OutputType.CANCEL_SUCCESS,
            data = ReminderOutput.ReminderData(
                index = index
            )
        )
    }
    
    private fun formatDuration(duration: Duration): String {
        return when {
            duration.toHours() >= 1 -> "${duration.toHours().toInt()} horas"
            duration.toMinutes() >= 1 -> "${duration.toMinutes().toInt()} minutos"
            else -> "${duration.toSeconds().toInt()} segundos"
        }
    }
    
    private fun calculateTimeRemaining(dueTimeMillis: Long): String {
        val remaining = dueTimeMillis - System.currentTimeMillis()
        
        return when {
            remaining <= 0 -> "ahora"
            remaining < 60_000 -> "en ${remaining / 1000} segundos"
            remaining < 3_600_000 -> "en ${remaining / 60_000} minutos"
            remaining < 86_400_000 -> "en ${remaining / 3_600_000} horas"
            else -> "en ${remaining / 86_400_000} días"
        }
    }
}
