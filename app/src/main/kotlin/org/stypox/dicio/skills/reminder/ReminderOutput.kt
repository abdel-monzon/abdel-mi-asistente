package org.stypox.dicio.skills.reminder

import org.dicio.skill.SkillContext
import org.dicio.skill.SkillOutput
import org.dicio.skill.util.CleanableUp
import org.stypox.dicio.util.getString

class ReminderOutput(
    private val messageResource: Int,
    private vararg val formatArgs: Any
) : SkillOutput, CleanableUp {

    override suspend fun generate(ctx: SkillContext): String {
        return ctx.getString(messageResource, *formatArgs)
    }

    override fun cleanup() {
        // Aquí cancelarías WorkManager si fuera necesario
    }
}

