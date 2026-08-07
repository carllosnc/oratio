package cnc.oratio.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import cnc.oratio.data.local.entity.ReminderEntity
import java.util.Calendar

object AlarmScheduler {

    const val EXTRA_REMINDER_ID = "extra_reminder_id"

    fun schedule(context: Context, reminder: ReminderEntity) {
        if (!reminder.isEnabled) {
            cancel(context, reminder.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = calculateNextTriggerTime(reminder)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancel(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun calculateNextTriggerTime(reminder: ReminderEntity): Long {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (reminder.isDaily) {
            if (calendar.before(now) || calendar.equals(now)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            // Weekly matching days
            val activeDays = reminder.daysOfWeek
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()

            // Convert Calendar.DAY_OF_WEEK (Sun=1, Mon=2..Sat=7) to 1..7 (Mon=1..Sun=7)
            fun calendarDayToIso(day: Int): Int = if (day == Calendar.SUNDAY) 7 else day - 1

            while (calendar.before(now) || calendar.equals(now) || !activeDays.contains(calendarDayToIso(calendar.get(Calendar.DAY_OF_WEEK)))) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return calendar.timeInMillis
    }
}
