package cnc.oratio.data.repository

import android.content.Context
import cnc.oratio.data.local.dao.PrayerDao
import cnc.oratio.data.local.dao.ReminderDao
import cnc.oratio.data.local.database.DatabaseInitializer
import cnc.oratio.data.local.database.OratioDatabase
import cnc.oratio.data.local.entity.CategoryEntity
import cnc.oratio.data.local.entity.LanguageEntity
import cnc.oratio.data.local.entity.PrayerLogEntity
import cnc.oratio.data.local.entity.ReminderEntity
import cnc.oratio.data.local.model.PrayerWithTranslations
import kotlinx.coroutines.flow.Flow

class PrayerRepository(
    private val context: Context,
    private val prayerDao: PrayerDao = OratioDatabase.getInstance(context).prayerDao(),
    private val reminderDao: ReminderDao = OratioDatabase.getInstance(context).reminderDao(),
    private val userPreferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context)
) {

    val userLanguageCode: Flow<String> = userPreferencesRepository.userLanguageCode

    suspend fun setUserLanguage(languageCode: String) {
        userPreferencesRepository.setUserLanguage(languageCode)
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

    fun getPrayerLogs(prayerId: String): Flow<List<String>> = prayerDao.getPrayerLogsForPrayer(prayerId)

    fun getAllPrayerLogs(): Flow<List<PrayerLogEntity>> = prayerDao.getAllPrayerLogs()

    suspend fun togglePrayerDate(prayerId: String, dateString: String, isCurrentlyMarked: Boolean) {
        if (isCurrentlyMarked) {
            prayerDao.deletePrayerLog(prayerId, dateString)
        } else {
            prayerDao.insertPrayerLog(PrayerLogEntity(prayerId, dateString))
        }
    }

    // Reminder Methods
    fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun getEnabledRemindersDirect(): List<ReminderEntity> = reminderDao.getEnabledRemindersDirect()

    suspend fun insertReminder(reminder: ReminderEntity): Long = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: ReminderEntity) = reminderDao.deleteReminder(reminder)

    suspend fun setReminderEnabled(id: Int, isEnabled: Boolean) = reminderDao.setReminderEnabled(id, isEnabled)
}
