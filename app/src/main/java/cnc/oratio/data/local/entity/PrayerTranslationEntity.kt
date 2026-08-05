package cnc.oratio.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "prayer_translations",
    primaryKeys = ["prayerId", "languageCode"],
    foreignKeys = [
        ForeignKey(
            entity = PrayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["prayerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LanguageEntity::class,
            parentColumns = ["code"],
            childColumns = ["languageCode"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["prayerId"]),
        Index(value = ["languageCode"])
    ]
)
data class PrayerTranslationEntity(
    val prayerId: String,
    val languageCode: String,
    val title: String,
    val subtitle: String? = null,
    val content: String,
    val notes: String? = null,
    val audioUrl: String? = null
)
