package org.dicio.skill.standard

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.dicio.numbers.parser.Parser
import org.dicio.numbers.util.Duration
import org.stypox.dicio.Sections
import org.stypox.dicio.skill.reminder.ReminderManager
import java.time.LocalDateTime

class ReminderSkill : StandardSkill() {
    private lateinit var manager: ReminderManager
    private lateinit var parser: Parser
    
    override fun setInput(input: String, result: StandardResult) {
        val context = getContext()
        manager = ReminderManager(context)
        
        // Obtener parser desde ParserFormatter (debe estar ya configurado)
        val parserFormatter = Sections.getParserFormatter()
        parser = parserFormatter?.parser ?: run {
            setOutput(StandardOutput("Error: No hay parser configurado"))
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
            setOutput(StandardOutput("¿Qué quieres que te recuerde?"))
            return
        }
        
        val timeExpression = result.getCapturingGroup("time") ?: run {
            setOutput(StandardOutput("¿Cuándo quieres que te lo recuerde?"))
            return
        }
        
        runBlocking {
            try {
                // Parsear duración usando dicio-numbers
                val duration: Duration = parser.parseDuration(timeExpression)
                    .orElseThrow { IllegalArgumentException("No se pudo parsear el tiempo") }
                
                // Convertir a milisegundos
                val triggerInMillis = (duration.toSeconds() * 1000).toLong()
                
                // Agregar recordatorio
                val id = manager.addReminder(text, triggerInMillis)
                
                setOutput(StandardOutput(
                    "✅ Recordatorio configurado: \"$text\" ${formatDuration(duration)}"
                ))
                
            } catch (e: Exception) {
                setOutput(StandardOutput(
                    "❌ No entendí el tiempo. Intenta algo como: " +
                    "\"recuérdame comprar leche en 2 horas\""
                ))
            }
        }
    }
    
    private fun handleListReminders() {
        manager.getAllReminders { reminders ->
            if (reminders.isEmpty()) {
                setOutput(StandardOutput("📭 No tienes recordatorios pendientes"))
                return@getAllReminders
            }
            
            val response = StringBuilder("📋 Tus recordatorios:\n")
            reminders.forEachIndexed { index, reminder ->
                response.append("${index + 1}. ${reminder.text} - ${reminder.getTimeRemaining()}\n")
            }
            
            setOutput(StandardOutput(response.toString()))
        }
    }
    
    private fun handleCancelReminder(result: StandardResult) {
        val indexStr = result.getCapturingGroup("index") ?: run {
            setOutput(StandardOutput("¿Qué recordatorio quieres cancelar?"))
            return
        }
        
        runBlocking {
            try {
                val index = indexStr.toInt() - 1  // Convertir a índice 0-based
                manager.getAllReminders { reminders ->
                    if (index in reminders.indices) {
                        val reminder = reminders[index]
                        manager.cancelReminder(reminder.id)
                        setOutput(StandardOutput("🗑️ Recordatorio \"${reminder.text}\" cancelado"))
                    } else {
                        setOutput(StandardOutput("❌ No encontré ese recordatorio"))
                    }
                }
            } catch (e: NumberFormatException) {
                setOutput(StandardOutput("❌ Por favor di un número, ejemplo: \"cancela el recordatorio 1\""))
            }
        }
    }
    
    private fun formatDuration(duration: Duration): String {
        return when {
            duration.toHours() >= 1 -> "en ${duration.toHours().toInt()} horas"
            duration.toMinutes() >= 1 -> "en ${duration.toMinutes().toInt()} minutos"
            else -> "en ${duration.toSeconds().toInt()} segundos"
        }
    }
}
