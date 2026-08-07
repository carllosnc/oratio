package cnc.oratio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cnc.oratio.data.local.entity.ReminderEntity
import cnc.oratio.data.local.model.PrayerWithTranslations
import cnc.oratio.data.repository.PrayerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RemindersUiState(
    val remindersState: List<ReminderEntity>? = null,
    val prayers: List<PrayerWithTranslations> = emptyList(),
    val userLanguageCode: String = "en"
)

class RemindersViewModel(
    private val repository: PrayerRepository
) : ViewModel() {

    val uiState: StateFlow<RemindersUiState> = combine(
        repository.getAllReminders(),
        repository.getAllPrayers(),
        repository.userLanguageCode
    ) { reminders, prayers, lang ->
        RemindersUiState(
            remindersState = reminders,
            prayers = prayers,
            userLanguageCode = lang
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RemindersUiState()
    )

    fun insertReminder(reminder: ReminderEntity, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insertReminder(reminder)
            onComplete(id)
        }
    }

    fun updateReminder(reminder: ReminderEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            onComplete()
        }
    }

    fun deleteReminder(reminder: ReminderEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            onComplete()
        }
    }

    fun setReminderEnabled(id: Int, isEnabled: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.setReminderEnabled(id, isEnabled)
            onComplete()
        }
    }
}
