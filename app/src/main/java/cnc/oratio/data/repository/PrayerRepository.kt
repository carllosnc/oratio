package cnc.oratio.data.repository

import android.content.Context
import cnc.oratio.data.local.dao.PrayerDao
import cnc.oratio.data.local.database.DatabaseInitializer
import cnc.oratio.data.local.database.OratioDatabase
import cnc.oratio.data.local.entity.CategoryEntity
import cnc.oratio.data.local.entity.LanguageEntity
import cnc.oratio.data.local.model.PrayerWithTranslations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PrayerRepository(
    private val context: Context,
    private val prayerDao: PrayerDao = OratioDatabase.getInstance(context).prayerDao()
) {

    private val prefs = context.getSharedPreferences("oratio_preferences", Context.MODE_PRIVATE)

    private val _userLanguageCode = MutableStateFlow(
        prefs.getString("user_language_code", "en") ?: "en"
    )
    val userLanguageCode: StateFlow<String> = _userLanguageCode.asStateFlow()

    fun setUserLanguage(languageCode: String) {
        prefs.edit().putString("user_language_code", languageCode).apply()
        _userLanguageCode.value = languageCode
    }

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
