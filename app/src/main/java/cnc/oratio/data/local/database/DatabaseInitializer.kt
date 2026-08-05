package cnc.oratio.data.local.database

import android.content.Context
import android.util.Log
import cnc.oratio.data.local.dao.PrayerDao
import cnc.oratio.data.local.entity.PrayerEntity
import cnc.oratio.data.local.entity.PrayerTranslationEntity
import cnc.oratio.data.local.model.SeedDataPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object DatabaseInitializer {

    private val jsonFormatter = Json {
        ignoreUnknownKeys = true
    }

    suspend fun populateIfEmpty(context: Context, prayerDao: PrayerDao) = withContext(Dispatchers.IO) {
        val existingCount = prayerDao.getPrayerCount()
        Log.d("DatabaseInitializer", "Existing prayer count: $existingCount")
        if (existingCount > 0) return@withContext

        try {
            val jsonString = context.assets.open("prayers_seed.json").bufferedReader().use { it.readText() }
            val seedData = jsonFormatter.decodeFromString<SeedDataPayload>(jsonString)

            prayerDao.insertLanguages(seedData.languages)
            prayerDao.insertCategories(seedData.categories)

            val prayerEntities = mutableListOf<PrayerEntity>()
            val translationEntities = mutableListOf<PrayerTranslationEntity>()

            seedData.prayers.forEach { seedItem ->
                prayerEntities.add(
                    PrayerEntity(
                        id = seedItem.id,
                        categoryId = seedItem.categoryId,
                        defaultTitle = seedItem.defaultTitle
                    )
                )

                seedItem.translations.forEach { tr ->
                    translationEntities.add(
                        PrayerTranslationEntity(
                            prayerId = seedItem.id,
                            languageCode = tr.languageCode,
                            title = tr.title,
                            subtitle = tr.subtitle,
                            content = tr.content,
                            notes = tr.notes,
                            audioUrl = tr.audioUrl
                        )
                    )
                }
            }

            prayerDao.insertPrayers(prayerEntities)
            prayerDao.insertTranslations(translationEntities)
            Log.d("DatabaseInitializer", "Successfully seeded database with ${prayerEntities.size} prayers!")
        } catch (e: Exception) {
            Log.e("DatabaseInitializer", "Error seeding database", e)
        }
    }
}
