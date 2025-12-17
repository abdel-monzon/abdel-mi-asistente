package org.stypox.dicio.skills.reminder

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.skill.Specificity
import org.dicio.skill.standard.StandardScore
import kotlin.random.Random

class ReminderSkill : Skill<Unit>(ReminderInfo, Specificity.HIGH) {
    
    override fun score(ctx: SkillContext, input: String): Pair<StandardScore, Unit> {
        // Detectar si la entrada es sobre recordatorios (igual que AgeSkill hace para "age")
        val reminderKeywords = listOf("remind", "recordar", "recordatorio", "recuerda", 
                                      "alarma", "reminder", "schedule", "agenda", 
                                      "cancel", "borrar", "eliminar", "list", "lista", 
                                      "muestra", "show", "avísame", "notifica")
        val containsReminderWord = reminderKeywords.any { keyword -> 
            input.contains(keyword, ignoreCase = true) 
        }
        
        return if (containsReminderWord) {
            // Score alto cuando detecta palabras relacionadas con recordatorios
            Pair(StandardScore(
                userMatched = 1.0f,
                userWeight = 1.0f, 
                refMatched = 1.0f,
                refWeight = 1.0f,
                capturingGroups = null
            ), Unit)
        } else {
            // Score bajo cuando no detecta palabras relacionadas
            Pair(StandardScore.EMPTY, Unit)
        }
    }
    
    override suspend fun generateOutput(ctx: SkillContext, inputData: Unit): SkillOutput {
        // Por ahora, devolvemos una salida simple para probar.
        // La lógica real para procesar "set", "list", "cancel" se implementará después.
        val responses = listOf(
            "He recibido tu solicitud de recordatorio. (Función en desarrollo)",
            "Los recordatorios estarán disponibles pronto.",
            "Entendí que quieres un recordatorio. Trabajando en ello..."
        )
        val randomResponse = responses[Random.nextInt(responses.size)]
        return ReminderOutput(randomResponse)
    }
}
