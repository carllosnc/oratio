package cnc.oratio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cnc.oratio.data.local.entity.CategoryEntity
import cnc.oratio.data.local.entity.LanguageEntity
import cnc.oratio.data.local.entity.PrayerLogEntity
import cnc.oratio.data.local.model.PrayerWithTranslations
import cnc.oratio.data.repository.PrayerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val prayersState: List<PrayerWithTranslations>? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val languages: List<LanguageEntity> = emptyList(),
    val userLanguageCode: String = "en",
    val allPrayerLogs: List<PrayerLogEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val showOnlyFavorites: Boolean = false,
    val showOnlyWithAudio: Boolean = false,
    val playingPrayerId: String? = null,
    val activeCalendarPrayerId: String? = null,
    val activeCalendarPrayerLogs: List<String> = emptyList()
) {
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() || selectedCategoryId != null || showOnlyFavorites || showOnlyWithAudio

    val filteredPrayers: List<PrayerWithTranslations>
        get() {
            val list = prayersState ?: return emptyList()
            val queryClean = searchQuery.trim().lowercase()

            return list.filter { item ->
                // Category Filter
                val matchesCategory = selectedCategoryId == null || item.prayer.categoryId == selectedCategoryId

                // Favorite Filter
                val matchesFavorite = !showOnlyFavorites || item.prayer.isFavorite

                // Audio Filter (All prayers have TTS fallback, but checking if audioUrl or valid content exists)
                val matchesAudio = !showOnlyWithAudio || item.translations.any { it.content.isNotBlank() }

                // Text Search Filter (Title, Subtitle, or Content across any translation)
                val matchesQuery = queryClean.isEmpty() || item.prayer.defaultTitle.lowercase().contains(queryClean) ||
                        item.translations.any { tr ->
                            tr.title.lowercase().contains(queryClean) ||
                                    (tr.subtitle?.lowercase()?.contains(queryClean) == true) ||
                                    tr.content.lowercase().contains(queryClean)
                        }

                matchesCategory && matchesFavorite && matchesAudio && matchesQuery
            }
        }
}

class HomeViewModel(
    private val repository: PrayerRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _showOnlyFavorites = MutableStateFlow(false)
    private val _showOnlyWithAudio = MutableStateFlow(false)
    private val _playingPrayerId = MutableStateFlow<String?>(null)
    private val _activeCalendarPrayerId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.initializeDatabaseIfNeeded()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val activeCalendarPrayerLogsFlow = _activeCalendarPrayerId.flatMapLatest { prayerId ->
        if (prayerId != null) {
            repository.getPrayerLogs(prayerId)
        } else {
            flowOf(emptyList())
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllPrayers(),
        repository.getAllCategories(),
        repository.getAllLanguages(),
        repository.userLanguageCode,
        repository.getAllPrayerLogs(),
        _searchQuery,
        _selectedCategoryId,
        _showOnlyFavorites,
        _showOnlyWithAudio,
        _playingPrayerId,
        _activeCalendarPrayerId,
        activeCalendarPrayerLogsFlow
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        HomeUiState(
            prayersState = args[0] as List<PrayerWithTranslations>?,
            categories = args[1] as List<CategoryEntity>,
            languages = args[2] as List<LanguageEntity>,
            userLanguageCode = args[3] as String,
            allPrayerLogs = args[4] as List<PrayerLogEntity>,
            searchQuery = args[5] as String,
            selectedCategoryId = args[6] as String?,
            showOnlyFavorites = args[7] as Boolean,
            showOnlyWithAudio = args[8] as Boolean,
            playingPrayerId = args[9] as String?,
            activeCalendarPrayerId = args[10] as String?,
            activeCalendarPrayerLogs = args[11] as List<String>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
    }

    fun toggleShowOnlyFavorites() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun toggleShowOnlyWithAudio() {
        _showOnlyWithAudio.value = !_showOnlyWithAudio.value
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedCategoryId.value = null
        _showOnlyFavorites.value = false
        _showOnlyWithAudio.value = false
    }

    fun setPlayingPrayerId(prayerId: String?) {
        _playingPrayerId.value = prayerId
        if (prayerId != null) {
            _activeCalendarPrayerId.value = null
        }
    }

    fun setActiveCalendarPrayerId(prayerId: String?) {
        _activeCalendarPrayerId.value = if (_activeCalendarPrayerId.value == prayerId) null else prayerId
    }

    fun closeCalendar() {
        _activeCalendarPrayerId.value = null
    }

    fun toggleFavorite(prayerId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(prayerId, isFavorite)
        }
    }

    fun setUserLanguage(languageCode: String) {
        viewModelScope.launch {
            setPlayingPrayerId(null)
            repository.setUserLanguage(languageCode)
        }
    }

    fun togglePrayerDate(prayerId: String, dateString: String, isMarked: Boolean) {
        viewModelScope.launch {
            repository.togglePrayerDate(prayerId, dateString, isMarked)
        }
    }
}
