package cnc.oratio.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "prayer_logs",
    primaryKeys = ["prayerId", "dateString"]
)
data class PrayerLogEntity(
    val prayerId: String,
    val dateString: String // Format: "YYYY-MM-DD"
)
