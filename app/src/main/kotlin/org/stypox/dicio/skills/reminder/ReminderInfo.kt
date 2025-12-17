package org.stypox.dicio.skills.reminder

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences

object ReminderInfo : SkillInfo("reminder") {
    override fun name(context: Context): String = 
        context.getString(R.string.skill_name_reminder)

    override fun sentenceExample(context: Context): String = 
        context.getString(R.string.skill_sentence_example_reminder)

    @Composable
    override fun icon() = rememberVectorPainter(Icons.Default.Alarm)

    override fun isAvailable(ctx: SkillContext): Boolean {
        return Sentences.Reminder[ctx.sentencesLanguage] != null
    }

    override fun build(ctx: SkillContext): Skill<*> {
        return ReminderSkill(
            this,
            Sentences.Reminder[ctx.sentencesLanguage]!!
        )
    }
}
