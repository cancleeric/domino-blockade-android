package com.cancleeric.dominoblockade.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.data.local.GameRecord
import com.cancleeric.dominoblockade.domain.repository.GameHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameHistoryViewModel @Inject constructor(
    private val repository: GameHistoryRepository
) : ViewModel() {

    val records: StateFlow<List<GameRecord>> = repository.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearAllRecords() {
        viewModelScope.launch {
            repository.clearAllRecords()
        }
    }
}
