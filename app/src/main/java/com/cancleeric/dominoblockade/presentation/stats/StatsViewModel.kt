package com.cancleeric.dominoblockade.presentation.stats

import androidx.lifecycle.ViewModel
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.domain.repository.PlayerStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val playerStatsRepository: PlayerStatsRepository
) : ViewModel() {

    val allStats: Flow<List<PlayerStatsEntity>> = playerStatsRepository.getAllStats()
}
