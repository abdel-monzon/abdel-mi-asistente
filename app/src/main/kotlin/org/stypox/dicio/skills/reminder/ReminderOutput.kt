package org.stypox.dicio.skills.reminder

import org.dicio.skill.SkillOutput
import org.dicio.skill.StandardResult

object ReminderOutput {
    fun success(message: String): SkillOutput = StandardResult.success(message)
    fun error(message: String): SkillOutput   = StandardResult.error(message)
}

