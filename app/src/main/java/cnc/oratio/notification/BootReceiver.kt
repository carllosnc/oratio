package cnc.oratio.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cnc.oratio.data.local.database.OratioDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = OratioDatabase.getInstance(context)
                    val enabledReminders = db.reminderDao().getEnabledRemindersDirect()

                    enabledReminders.forEach { reminder ->
                        AlarmScheduler.schedule(context, reminder)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
