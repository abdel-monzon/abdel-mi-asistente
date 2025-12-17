package org.stypox.dicio.skills.reminder

import org.dicio.skill.standard.StandardResult

/**
 * Representa los diferentes tipos de entrada que puede recibir la habilidad.
 */
sealed class ReminderInput {
    /** Cuando el usuario pide listar recordatorios */
    object List : ReminderInput()
    
    /** Cuando el usuario pide establecer un recordatorio */
    data class Set(
        val text: String,
        val timeExpression: String
    ) : ReminderInput()
    
    /** Cuando el usuario pide cancelar un recordatorio */
    data class Cancel(
        val index: Int
    ) : ReminderInput()
    
    companion object {
        /**
         * Crea un ReminderInput a partir de un StandardResult.
         * Devuelve null si no se puede interpretar.
         */
        fun fromStandardResult(result: StandardResult): ReminderInput? {
            return when (result.sentenceId) {
                "set" -> {
                    val text = result.getCapturingGroup("text") ?: return null
                    val time = result.getCapturingGroup("time") ?: return null
                    Set(text, time)
                }
                "list" -> List
                "cancel" -> {
                    val indexStr = result.getCapturingGroup("index") ?: return null
                    val index = indexStr.toIntOrNull() ?: return null
                    Cancel(index)
                }
                else -> null
            }
        }
    }
}
