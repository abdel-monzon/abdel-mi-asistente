package org.stypox.dicio.skills.reminder

import android.content.Context
import org.dicio.skill.standard.StandardSkill
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillType
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences.Reminder

class ReminderInfo : SkillInfo {
    override val id: String = "reminder"
    override val name: String = "Recordatorio"
    override val icon: Int = R.drawable.ic_alarm
    override val type: SkillType = SkillType.ACTION
    
    override fun isAvailable(context: Context): Boolean = true
    
    override fun build(context: org.dicio.skill.skill.SkillContext): StandardSkill<Reminder> {
        return ReminderSkill(this, StandardSkill.StandardRecognizerData(Reminder::class))
    }
}
