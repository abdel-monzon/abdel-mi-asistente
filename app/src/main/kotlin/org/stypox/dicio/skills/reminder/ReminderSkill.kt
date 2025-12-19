package org.stypox.dicio.skills.reminder

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.flow.first
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.sentences.Sentences.Reminder
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

class ReminderSkill(
    correspondingSkillInfo: ReminderInfo,
    data: StandardRecognizerData<Reminder>
) : StandardRecognizerSkill<Reminder>(correspondingSkillInfo, data) {

    private lateinit var repository: ReminderRepository
    private lateinit var workManager: WorkManager

    override suspend fun setup(context: SkillContext) {
        super.setup(context)
        repository = ReminderRepository(context.android)
        workManager = WorkManager.getInstance(context.android)
    }

    override suspend fun generateOutput(ctx: SkillContext, inputData: Reminder): SkillOutput {
        return when (inputData) {
            is Reminder.Set -> handleSetReminder(ctx, inputData)
            is Reminder.List -> handleListReminders(ctx)
            is Reminder.Cancel -> handleCancelReminder(ctx, inputData)
        }
    }
    
    private suspend fun handleSetReminder(ctx: SkillContext, input: Reminder.Set): SkillOutput {
        val context = ctx.android
        
        if (input.text.isNullOrBlank()) {
            return ReminderOutput.Error(
                message = context.getString(org.stypox.dicio.R.string.skill_reminder_set_error_no_text)
            )
        }
        
        if (input.time.isNullOrBlank()) {
            return ReminderOutput.Error(
                message = context.getString(org.stypox.dicio.R.string.skill_reminder_set_error_no_time)
            )
        }
        
        // Parsear tiempo natural
        val reminderTime = TimeParser.parseTimeExpression(input.time!!)
        
        if (reminderTime == null) {
            return ReminderOutput.Error(
                message = "No entendí el tiempo: '${input.time}'. Prueba diciendo 'en 2 horas' o 'mañana a las 3'"
            )
        }
        
        // Guardar en base de datos
        val id = repository.addReminder(input.text!!, reminderTime)
        
        // Programar alarma con WorkManager
        scheduleReminderWork(context, id, input.text!!, reminderTime)
        
        // Formatear tiempo restante para respuesta
        val timeRemaining = repository.formatTimeRemaining(reminderTime)
        
        return ReminderOutput.SetSuccess(
            text = input.text!!,
            timeRemaining = timeRemaining
        )
    }
    
    private suspend fun handleListReminders(ctx: SkillContext): SkillOutput {
        val reminders = repository.getAllActiveReminders().first()
        
        return if (reminders.isEmpty()) {
            ReminderOutput.ListEmpty
        } else {
            val reminderItems = reminders.map { reminder ->
                ReminderOutput.ReminderItem(
                    id = reminder.id,
                    text = reminder.text,
                    timeRemaining = repository.formatTimeRemaining(reminder.timestamp)
                )
            }
            ReminderOutput.ListSuccess(reminderItems)
        }
    }
    
    private suspend fun handleCancelReminder(ctx: SkillContext, input: Reminder.Cancel): SkillOutput {
        val index = input.index?.toIntOrNull()
        
        if (index == null) {
            return ReminderOutput.Error(
                message = ctx.android.getString(
                    org.stypox.dicio.R.string.skill_reminder_cancel_error_no_number
                )
            )
        }
        
        val reminder = repository.getReminderById(index)
        if (reminder == null) {
            return ReminderOutput.Error(
                message = "No encontré el recordatorio número $index"
            )
        }
        
        // Cancelar en base de datos
        repository.cancelReminder(index)
        
        // Cancelar trabajo de WorkManager
        workManager.cancelUniqueWork("reminder_$index")
        
        return ReminderOutput.CancelSuccess(index = index)
    }
    
    private fun scheduleReminderWork(
        context: Context,
        reminderId: Long,
        text: String,
        timestamp: Instant
    ) {
        val delay = Duration.between(Instant.now(), timestamp).seconds
        
        if (delay > 0) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val inputData = Data.Builder()
                .putLong("REMINDER_ID", reminderId)
                .putString("REMINDER_TEXT", text)
                .build()
            
            val reminderWork = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag("reminder_$reminderId")
                .build()
            
            workManager.enqueueUniqueWork(
                "reminder_$reminderId",
                ExistingWorkPolicy.REPLACE,
                reminderWork
            )
        }
    }
}
