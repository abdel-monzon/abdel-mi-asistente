package org.stypox.dicio.skills.reminder

import org.dicio.skill.SkillContext
import org.dicio.skill.SkillOutput
import org.stypox.dicio.R

class ReminderOutput(
    private val messageRes: Int,
    private vararg val args: Any
) : SkillOutput {

    override suspend fun generate(ctx: SkillContext): String {
        return ctx.android.getString(messageRes, *args)
    }
}

