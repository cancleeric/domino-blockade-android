package com.cancleeric.dominoblockade.presentation.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.model.QuestDashboard
import com.cancleeric.dominoblockade.domain.repository.QuestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestViewModel @Inject constructor(
    private val questRepository: QuestRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    val dashboard: StateFlow<QuestDashboard> = questRepository.observeDashboard()
        .stateIn(viewModelScope, SharingStarted.Eagerly, QuestDashboard())

    init {
        viewModelScope.launch {
            runCatching { questRepository.refresh() }
                .onFailure { _events.emit("Using offline challenge cache") }
        }
    }

    fun claimReward(taskId: String) {
        viewModelScope.launch {
            val reward = questRepository.claimReward(taskId)
            if (reward != null) {
                val suffix = reward.achievementUnlocked?.title?.let { ", Achievement: $it" }.orEmpty()
                _events.emit("Reward claimed: +${reward.coinsAwarded} coins, +${reward.xpAwarded} XP$suffix")
            }
        }
    }
}
