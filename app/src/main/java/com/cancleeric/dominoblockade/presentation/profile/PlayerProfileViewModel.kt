package com.cancleeric.dominoblockade.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.domain.model.PlayerProfile
import com.cancleeric.dominoblockade.domain.repository.PlayerProfileRepository
import com.cancleeric.dominoblockade.domain.repository.PlayerStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerProfileViewModel @Inject constructor(
    private val profileRepository: PlayerProfileRepository,
    private val statsRepository: PlayerStatsRepository
) : ViewModel() {

    val profile: StateFlow<PlayerProfile> = profileRepository.getProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlayerProfile())

    val stats: StateFlow<PlayerStatsEntity?> = profileRepository.getProfile()
        .flatMapLatest { p -> flow { emit(statsRepository.getByName(p.playerName)) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun saveName(name: String) {
        viewModelScope.launch {
            profileRepository.saveProfile(profile.value.copy(playerName = name.trim()))
        }
    }

    fun saveAvatar(emoji: String) {
        viewModelScope.launch {
            profileRepository.saveProfile(profile.value.copy(avatarEmoji = emoji))
        }
    }

}
