package cnc.oratio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "languages")
data class LanguageEntity(
    @PrimaryKey val code: String, // ex: "la", "pt", "en", "es"
    val name: String,             // ex: "Latina", "Português"
    val flagIcon: String          // ex: "🌐", "🇧🇷"
)
