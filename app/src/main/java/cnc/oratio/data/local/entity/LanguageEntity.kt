package cnc.oratio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "languages")
data class LanguageEntity(
    @PrimaryKey val code: String, // e.g., "la", "pt", "en", "es"
    val name: String,             // e.g., "Latina", "Portuguese"
    val flagIcon: String          // e.g., "🌐", "🇧🇷"
)
