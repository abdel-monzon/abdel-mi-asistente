package org.stypox.dicio.skills.reminder

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.skill.Specificity
import org.dicio.skill.standard.StandardScore
import org.stypox.dicio.sentences.Sentences
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class ReminderSkill : Skill<Unit>(ReminderInfo, Specificity.HIGH) {

    override fun score(ctx: SkillContext, input: String): Pair<StandardScore, Unit> {
        // 1. Obtener los datos del reconocedor para el idioma actual
        val recognizerData = Sentences.Reminder[ctx.sentencesLanguage]

        return if (recognizerData != null) {
            // 2. Usarlo para evaluar la entrada del usuario
            val result = recognizerData.score(input)

            // 3. Devolver la puntuación junto con Unit como datos de entrada
            Pair(result.score, Unit)
        } else {
            // Si no hay datos del reconocedor, devolver una puntuación vacía
            Pair(StandardScore.EMPTY, Unit)
        }
    }

    override suspend fun generateOutput(ctx: SkillContext, inputData: Unit): SkillOutput {
        // Por ahora, devolvemos una salida simple para probar.
        // Más adelante implementaremos la lógica real (añadir, listar, cancelar).
        val responseText = "La función de recordatorio está funcionando. (Por implementar: lógica para configurar, listar y cancelar recordatorios)"
        return ReminderOutput(responseText)
    }
}
