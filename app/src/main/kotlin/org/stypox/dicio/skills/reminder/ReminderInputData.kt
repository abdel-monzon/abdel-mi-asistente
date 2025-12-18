
package org.stypox.dicio.skills.reminder

import org.dicio.skill.SkillData

data class ReminderInputData(
    val sentenceId: String,
    val text: String?,
    val time: String?,
    val index: String?
) : SkillData
