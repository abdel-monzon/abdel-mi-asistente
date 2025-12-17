package org.stypox.dicio.skills.reminder

import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.util.Score
import org.stypox.dicio.Sections
import org.stypox.dicio.R
import org.stypox.dicio.util.Duration
import org.stypox.dicio.util.StringUtils

class ReminderSkill : Skill<ReminderInputData>() {

    override suspend fun generateOutput(
        ctx: SkillContext,
        inputData: ReminderInputData
    ): ReminderOutput {
        return when (inputData.sentenceId) {
            "set"    -> handleSet(ctx, inputData)
            "list"   -> handleList(ctx)
            "cancel" -> handleCancel(ctx, inputData)
            else     -> ReminderOutput(R.string.skill_error_unknown_intent)
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

    private fun handleSet(ctx: SkillContext, data: ReminderInputData): ReminderOutput {
        val text = data.text ?: return ReminderOutput(R.string.skill_error_missing_text)
        val time = data.time ?: return ReminderOutput(R.string.skill_error_missing_time)
        
        // TODO: parsear "time" con Duration.parse() y programar WorkManager
        return ReminderOutput(R.string.skill_reminder_set_success, text, time)
    }

    private fun handleList(ctx: SkillContext): ReminderOutput {
        // TODO: leer de la base de datos
        return ReminderOutput(R.string.skill_reminder_list_empty)
    }

    private fun handleCancel(ctx: SkillContext, data: ReminderInputData): ReminderOutput {
        val idx = data.index?.toIntOrNull() ?: return ReminderOutput(R.string.skill_error_invalid_index)
        
        // TODO: cancelar en WorkManager y BD
        return ReminderOutput(R.string.skill_reminder_cancel_success, idx)
    }
}

