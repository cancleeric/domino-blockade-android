package com.cancleeric.dominoblockade.presentation.history

import androidx.lifecycle.ViewModel
import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import com.cancleeric.dominoblockade.domain.repository.GameRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val gameRecordRepository: GameRecordRepository
) : ViewModel() {

    val records: Flow<List<GameRecordEntity>> = gameRecordRepository.getAllRecords()
}
