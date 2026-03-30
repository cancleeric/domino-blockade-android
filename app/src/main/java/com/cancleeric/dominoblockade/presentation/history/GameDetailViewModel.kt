package com.cancleeric.dominoblockade.presentation.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.cancleeric.dominoblockade.data.local.GameRecord
import com.cancleeric.dominoblockade.domain.repository.GameHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: GameHistoryRepository
) : ViewModel() {
    private val recordId: Long = checkNotNull(savedStateHandle["recordId"])
    val record: Flow<GameRecord?> = repository.getRecordById(recordId)
}
