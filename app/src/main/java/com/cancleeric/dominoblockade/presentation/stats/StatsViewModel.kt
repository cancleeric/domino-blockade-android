package com.cancleeric.dominoblockade.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.model.GameStats
import com.cancleeric.dominoblockade.domain.repository.GameHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: GameHistoryRepository
) : ViewModel() {

    val stats: StateFlow<GameStats> = repository.getStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameStats())
}
