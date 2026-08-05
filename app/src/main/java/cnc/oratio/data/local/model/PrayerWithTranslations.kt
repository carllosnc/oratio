package cnc.oratio.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import cnc.oratio.data.local.entity.PrayerEntity
import cnc.oratio.data.local.entity.PrayerTranslationEntity

data class PrayerWithTranslations(
    @Embedded val prayer: PrayerEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "prayerId"
    )
    val translations: List<PrayerTranslationEntity>
)
