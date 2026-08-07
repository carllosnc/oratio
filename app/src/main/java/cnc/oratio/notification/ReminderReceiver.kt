package cnc.oratio.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cnc.oratio.data.local.database.OratioDatabase
import cnc.oratio.data.repository.PrayerRepository
import cnc.oratio.ui.util.UiStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra(AlarmScheduler.EXTRA_REMINDER_ID, -1)
        if (reminderId == -1) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = OratioDatabase.getInstance(context)
                val reminder = db.reminderDao().getReminderById(reminderId)

                if (reminder != null && reminder.isEnabled) {
                    val prefs = context.getSharedPreferences("oratio_preferences", Context.MODE_PRIVATE)
                    val langCode = prefs.getString("user_language_code", "en") ?: "en"

                    val repository = PrayerRepository(context, db.prayerDao(), db.reminderDao())
                    val prayers = repository.getAllPrayers().firstOrNull() ?: emptyList()

                    val targetPrayer = if (!reminder.prayerId.isNull_or_blank()) {
                        prayers.find { it.prayer.id == reminder.prayerId }
                    } else {
                        prayers.randomOrNull() ?: prayers.firstOrNull()
                    }

                    if (targetPrayer != null) {
                        val translation = targetPrayer.translations.find { it.languageCode == langCode }
                            ?: targetPrayer.translations.find { it.languageCode == "en" }
                            ?: targetPrayer.translations.firstOrNull()

                        val title = when {
                            reminder.label.isNotBlank() -> reminder.label
                            translation != null -> translation.title
                            else -> targetPrayer.prayer.defaultTitle
                        }

                        val snippetText = translation?.content
                            ?.replace("\n", " ")
                            ?.replace(Regex("\\s+"), " ")
                            ?.take(110)
                            ?: "Hora da sua oração diária."

                        val fullMessage = if (snippetText.length >= 110) "$snippetText..." else snippetText

                        NotificationHelper.showNotification(
                            context = context,
                            notificationId = reminder.id,
                            title = "Oratio • $title",
                            message = fullMessage,
                            prayerId = targetPrayer.prayer.id
                        )
                    } else {
                        NotificationHelper.showNotification(
                            context = context,
                            notificationId = reminder.id,
                            title = "Oratio • Hora da Oração",
                            message = "Tire um momento de paz para a sua oração diária.",
                            prayerId = null
                        )
                    }

                    // Re-schedule next occurrence
                    AlarmScheduler.schedule(context, reminder)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
}
