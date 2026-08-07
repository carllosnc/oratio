package cnc.oratio.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val USER_LANGUAGE_KEY = stringPreferencesKey("user_language_code")
    }

    val userLanguageCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_LANGUAGE_KEY] ?: "en"
    }

    suspend fun setUserLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_LANGUAGE_KEY] = languageCode
        }
    }
}
