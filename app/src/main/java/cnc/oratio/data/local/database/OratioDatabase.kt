package cnc.oratio.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cnc.oratio.data.local.dao.PrayerDao
import cnc.oratio.data.local.entity.CategoryEntity
import cnc.oratio.data.local.entity.LanguageEntity
import cnc.oratio.data.local.entity.PrayerEntity
import cnc.oratio.data.local.entity.PrayerLogEntity
import cnc.oratio.data.local.entity.PrayerTranslationEntity

@Database(
    entities = [
        LanguageEntity::class,
        CategoryEntity::class,
        PrayerEntity::class,
        PrayerTranslationEntity::class,
        PrayerLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OratioDatabase : RoomDatabase() {

    abstract fun prayerDao(): PrayerDao

    companion object {
        @Volatile
        private var INSTANCE: OratioDatabase? = null

        fun getInstance(context: Context): OratioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OratioDatabase::class.java,
                    "oratio_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
