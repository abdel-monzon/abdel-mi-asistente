package org.stypox.dicio.skills.reminder

import org.dicio.skill.context.SkillContext
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput

class ReminderOutput(private val speechText: String) : HeadlineSpeechSkillOutput {

    override fun getSpeechOutput(ctx: SkillContext): String {
        // Simplemente devuelve el texto que se pasó en el constructor.
        // Más adelante podrás expandir esto para manejar diferentes tipos de salida.
        return speechText
    }
}
