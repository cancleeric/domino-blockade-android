package com.cancleeric.dominoblockade.presentation.achievements

import androidx.lifecycle.ViewModel
import com.cancleeric.dominoblockade.domain.model.Achievement
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    repository: AchievementRepository
) : ViewModel() {

    val achievements: StateFlow<List<Achievement>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    companion object {
        private const val STOP_TIMEOUT_MS = 5000L
    }
}
