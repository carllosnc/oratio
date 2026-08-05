package cnc.oratio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import cnc.oratio.data.local.entity.CategoryEntity
import cnc.oratio.data.local.entity.LanguageEntity
import cnc.oratio.data.local.entity.PrayerEntity
import cnc.oratio.data.local.entity.PrayerTranslationEntity
import cnc.oratio.data.local.model.PrayerWithTranslations
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguages(languages: List<LanguageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayers(prayers: List<PrayerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<PrayerTranslationEntity>)

    @Query("SELECT COUNT(*) FROM prayers")
    suspend fun getPrayerCount(): Int

    @Query("SELECT * FROM languages")
    fun getAllLanguages(): Flow<List<LanguageEntity>>

    @Query("SELECT * FROM categories ORDER BY displayOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Transaction
    @Query("SELECT * FROM prayers")
    fun getAllPrayersWithTranslations(): Flow<List<PrayerWithTranslations>>

    @Transaction
    @Query("SELECT * FROM prayers WHERE id = :prayerId")
    fun getPrayerById(prayerId: String): Flow<PrayerWithTranslations?>

    @Transaction
    @Query("SELECT * FROM prayers WHERE categoryId = :categoryId")
    fun getPrayersByCategory(categoryId: String): Flow<List<PrayerWithTranslations>>

    @Transaction
    @Query("SELECT * FROM prayers WHERE isFavorite = 1")
    fun getFavoritePrayers(): Flow<List<PrayerWithTranslations>>

    @Query("UPDATE prayers SET isFavorite = :isFavorite WHERE id = :prayerId")
    suspend fun setFavorite(prayerId: String, isFavorite: Boolean)

    @Transaction
    @Query("""
        SELECT DISTINCT p.* FROM prayers p
        INNER JOIN prayer_translations t ON p.id = t.prayerId
        WHERE t.title LIKE '%' || :query || '%' 
           OR t.content LIKE '%' || :query || '%'
    """)
    fun searchPrayers(query: String): Flow<List<PrayerWithTranslations>>
}
