package cnc.oratio.data.repository

import android.content.Context
import cnc.oratio.data.local.dao.PrayerDao
import cnc.oratio.data.local.database.DatabaseInitializer
import cnc.oratio.data.local.database.OratioDatabase
import cnc.oratio.data.local.entity.CategoryEntity
import cnc.oratio.data.local.entity.LanguageEntity
import cnc.oratio.data.local.model.PrayerWithTranslations
import kotlinx.coroutines.flow.Flow

class PrayerRepository(
    private val context: Context,
    private val prayerDao: PrayerDao = OratioDatabase.getInstance(context).prayerDao()
) {

    suspend fun initializeDatabaseIfNeeded() {
        DatabaseInitializer.populateIfEmpty(context, prayerDao)
    }

    fun getAllLanguages(): Flow<List<LanguageEntity>> = prayerDao.getAllLanguages()

    fun getAllCategories(): Flow<List<CategoryEntity>> = prayerDao.getAllCategories()

    fun getAllPrayers(): Flow<List<PrayerWithTranslations>> = prayerDao.getAllPrayersWithTranslations()

    fun getPrayerById(id: String): Flow<PrayerWithTranslations?> = prayerDao.getPrayerById(id)

    fun getPrayersByCategory(categoryId: String): Flow<List<PrayerWithTranslations>> =
        prayerDao.getPrayersByCategory(categoryId)

    fun getFavoritePrayers(): Flow<List<PrayerWithTranslations>> = prayerDao.getFavoritePrayers()

    suspend fun toggleFavorite(prayerId: String, isFavorite: Boolean) {
        prayerDao.setFavorite(prayerId, isFavorite)
    }

    fun searchPrayers(query: String): Flow<List<PrayerWithTranslations>> = prayerDao.searchPrayers(query)
}
