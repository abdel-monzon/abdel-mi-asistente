package org.stypox.dicio.skills.reminder

data class ReminderInputData(
    val sentenceId: String,
    val text: String?,
    val time: String?,
    val index: String?
)
