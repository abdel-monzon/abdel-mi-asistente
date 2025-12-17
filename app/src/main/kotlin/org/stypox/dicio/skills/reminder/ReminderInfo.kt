package org.stypox.dicio.skills.reminder

import org.dicio.skill.SkillInfo
import org.dicio.skill.SkillType
import org.stypox.dicio.R

object ReminderInfo : SkillInfo(
    "reminder",                                    // id del skill
    R.string.skill_name_reminder,                  // string resource del nombre
    R.string.skill_sentence_example_reminder,      // ejemplo de frase
    R.drawable.ic_alarm,                           // icono
    false,                                         // no requiere datos
    SkillType.DYNAMIC                              // tipo dinámico
)

