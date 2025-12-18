package org.stypox.dicio.skills.reminder

import org.dicio.skill.Skill
import org.dicio.skill.SkillContext
import org.dicio.skill.util.Score
import org.stypox.dicio.Sections

class ReminderSkill : Skill<ReminderInputData>() {

    override suspend fun generateOutput(
        ctx: SkillContext,
        inputData: ReminderInputData
    ): ReminderOutput {
        return when (inputData.sentenceId) {
            "set" -> ReminderOutput(
                R.string.skill_reminder_set_success,
                inputData.text ?: "",
                inputData.time ?: ""
            )
            "list" -> ReminderOutput(R.string.skill_reminder_list_empty)
            "cancel" -> ReminderOutput(
                R.string.skill_reminder_cancel_success,
                inputData.index?.toIntOrNull() ?: 0
            )
            else -> ReminderOutput(R.string.skill_error_unknown_intent)
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
}
