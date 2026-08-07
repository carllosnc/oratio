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
    val selectedCategoryId: String? = null,
    val showOnlyFavorites: Boolean = false,
    val playingPrayerId: String? = null,
    val activeCalendarPrayerId: String? = null,
    val activeCalendarPrayerLogs: List<String> = emptyList()
) {
    val filteredPrayers: List<PrayerWithTranslations>
        get() {
            val list = prayersState ?: return emptyList()
            return list.filter { item ->
                val matchesCategory = selectedCategoryId == null || item.prayer.categoryId == selectedCategoryId
                val matchesFavorite = !showOnlyFavorites || item.prayer.isFavorite
                matchesCategory && matchesFavorite
            }
        }
}

class HomeViewModel(
    private val repository: PrayerRepository
) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _showOnlyFavorites = MutableStateFlow(false)
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
        _selectedCategoryId,
        _showOnlyFavorites,
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
            selectedCategoryId = args[5] as String?,
            showOnlyFavorites = args[6] as Boolean,
            playingPrayerId = args[7] as String?,
            activeCalendarPrayerId = args[8] as String?,
            activeCalendarPrayerLogs = args[9] as List<String>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
        _showOnlyFavorites.value = false
    }

    fun toggleShowOnlyFavorites() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
        if (_showOnlyFavorites.value) {
            _selectedCategoryId.value = null
        }
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
