package org.stypox.dicio.skills.reminder

import org.dicio.skill.SkillInfo
import org.dicio.skill.SkillType
import org.stypox.dicio.R

object ReminderInfo : SkillInfo(
    id = "reminder",
    nameRes = R.string.skill_name_reminder,
    sentenceExampleRes = R.string.skill_sentence_example_reminder,
    iconRes = R.drawable.ic_alarm,
    needsData = false,
    skillType = SkillType.DYNAMIC
)
