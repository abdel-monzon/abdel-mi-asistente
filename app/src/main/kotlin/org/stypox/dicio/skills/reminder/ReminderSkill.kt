
package org.stypox.dicio.skills.reminder

import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.SkillInfo
import org.dicio.skill.SkillOutput
import org.dicio.skill.StandardResult
import org.dicio.skill.util.SentenceInfo
import org.dicio.skill.util.Score
import org.stypox.dicio.Sections
import org.stypox.dicio.util.Duration
import org.stypox.dicio.util.StringUtils

class ReminderSkill : Skill<ReminderInputData>() {

    override suspend fun generateOutput(
        ctx: SkillContext,
        inputData: ReminderInputData
    ): SkillOutput {
        return when (inputData.sentenceId) {
            "set"    -> handleSet(ctx, inputData)
            "list"   -> handleList(ctx)
            "cancel" -> handleCancel(ctx, inputData)
            else     -> StandardResult.error("Unknown intent")
        }
    }

    override fun score(ctx: SkillContext, input: String): Pair<Score, ReminderInputData?> {
        val sentence = Sections.getSentence(ctx, "reminder", input) ?: return Score.MIN to null
        return Score.MAX to ReminderInputData(
            sentenceId = sentence.sentenceId,
            text = sentence.getCapturingGroup("text"),
            time = sentence.getCapturingGroup("time"),
            index = sentence.getCapturingGroup("index")
        )
    }

    /* ---------- handlers privados ---------- */
    private fun handleSet(ctx: SkillContext, data: ReminderInputData): SkillOutput {
        // TODO: parsear *time* y programar WorkManager
        return StandardResult.success("Recordatorio guardado: “${data.text}”")
    }

    private fun handleList(ctx: SkillContext): SkillOutput {
        // TODO: leer lista de BD/SharedPrefs
        return StandardResult.success("No hay recordatorios activos.")
    }

    private fun handleCancel(ctx: SkillContext, data: ReminderInputData): SkillOutput {
        // TODO: cancelar por *index*
        return StandardResult.success("Recordatorio cancelado.")
    }
}
