package cnc.oratio.data.local.model

import cnc.oratio.data.local.entity.CategoryEntity
import cnc.oratio.data.local.entity.LanguageEntity
import kotlinx.serialization.Serializable

@Serializable
data class SeedPrayerTranslation(
    val languageCode: String,
    val title: String,
    val subtitle: String? = null,
    val content: String,
    val notes: String? = null,
    val audioUrl: String? = null
)

@Serializable
data class SeedPrayerItem(
    val id: String,
    val categoryId: String,
    val defaultTitle: String,
    val translations: List<SeedPrayerTranslation>
)

@Serializable
data class SeedDataPayload(
    val languages: List<LanguageEntity>,
    val categories: List<CategoryEntity>,
    val prayers: List<SeedPrayerItem>
)
