package cnc.oratio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val prayerId: String? = null, // null means "Random / Daily Featured Prayer"
    val hour: Int,                // 0 to 23
    val minute: Int,              // 0 to 59
    val isDaily: Boolean = true,  // true = daily, false = specific days of week
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1 (Mon) .. 7 (Sun)
    val isEnabled: Boolean = true,
    val label: String = ""
)
