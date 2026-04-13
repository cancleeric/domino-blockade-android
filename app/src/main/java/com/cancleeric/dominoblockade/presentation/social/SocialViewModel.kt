package com.cancleeric.dominoblockade.presentation.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cancleeric.dominoblockade.domain.model.ChallengeInvitation
import com.cancleeric.dominoblockade.domain.model.Friend
import com.cancleeric.dominoblockade.domain.model.FriendRequest
import com.cancleeric.dominoblockade.domain.repository.SocialRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val DEFAULT_GAME_MODE = "Classic"

data class SocialUiState(
    val username: String = "",
    val qrValue: String = "",
    val searchQuery: String = "",
    val searchResults: List<Friend> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val incomingFriendRequests: List<FriendRequest> = emptyList(),
    val pendingChallenges: List<ChallengeInvitation> = emptyList(),
    val isSavingUsername: Boolean = false,
    val error: String? = null,
    val navigateToLobbyRoomId: String? = null
)

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val firebaseMessaging: FirebaseMessaging
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            runCatching {
                val uid = socialRepository.ensureSignedIn()
                socialRepository.ensureUserProfile("Player-${uid.shortId()}")
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message ?: "Unable to authenticate user") }
            }
        }
        viewModelScope.launch {
            socialRepository.observeMyUsername().collect { username ->
                _uiState.update { state -> state.copy(username = username) }
            }
        }
        viewModelScope.launch {
            socialRepository.observeMyQrValue().collect { qrValue ->
                _uiState.update { state -> state.copy(qrValue = qrValue) }
            }
        }
        viewModelScope.launch {
            socialRepository.observeFriends().collect { friends ->
                _uiState.update { state -> state.copy(friends = friends) }
            }
        }
        viewModelScope.launch {
            socialRepository.observeIncomingFriendRequests().collect { requests ->
                _uiState.update { state -> state.copy(incomingFriendRequests = requests) }
            }
        }
        viewModelScope.launch {
            socialRepository.observePendingChallenges().collect { challenges ->
                _uiState.update { state -> state.copy(pendingChallenges = challenges) }
            }
        }
        viewModelScope.launch {
            runCatching { socialRepository.updatePresence(true) }
        }
        viewModelScope.launch {
            runCatching {
                val token = firebaseMessaging.token.await()
                socialRepository.saveFcmToken(token)
            }
        }
    }

    fun setUsername(username: String) {
        _uiState.update { it.copy(username = username, error = null) }
    }

    fun saveUsername() {
        val username = _uiState.value.username.trim()
        if (username.isEmpty()) {
            _uiState.update { it.copy(error = "Username cannot be empty") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingUsername = true, error = null) }
            runCatching { socialRepository.ensureUserProfile(username) }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message ?: "Failed to save username") }
                }
            _uiState.update { it.copy(isSavingUsername = false) }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, error = null) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            socialRepository.searchPlayers(query).collect { results ->
                _uiState.update { state -> state.copy(searchResults = results) }
            }
        }
    }

    fun sendFriendRequest(targetUid: String) {
        viewModelScope.launch {
            runCatching { socialRepository.sendFriendRequest(targetUid) }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message ?: "Failed to send friend request") }
                }
        }
    }

    fun respondToFriendRequest(requestId: String, accept: Boolean) {
        viewModelScope.launch {
            runCatching { socialRepository.respondToFriendRequest(requestId, accept) }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message ?: "Failed to update friend request") }
                }
        }
    }

    fun sendChallenge(friend: Friend, gameMode: String = DEFAULT_GAME_MODE) {
        viewModelScope.launch {
            runCatching { socialRepository.sendChallenge(friend.uid, friend.username, gameMode) }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message ?: "Failed to send challenge") }
                }
        }
    }

    fun respondToChallenge(challengeId: String, accept: Boolean) {
        viewModelScope.launch {
            runCatching { socialRepository.respondToChallenge(challengeId, accept) }
                .onSuccess { roomId ->
                    if (accept && !roomId.isNullOrBlank()) {
                        _uiState.update { it.copy(navigateToLobbyRoomId = roomId) }
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message ?: "Failed to update challenge") }
                }
        }
    }

    fun onQrScanned(rawValue: String) {
        val uid = parseFriendUidFromQr(rawValue) ?: return
        viewModelScope.launch {
            runCatching { socialRepository.addFriendByQr(uid) }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message ?: "Failed to add friend from QR") }
                }
        }
    }

    fun applyChallengeAction(challengeId: String?, action: String?) {
        val id = challengeId.orEmpty()
        if (id.isBlank()) return
        when (action) {
            "accept" -> respondToChallenge(id, true)
            "decline" -> respondToChallenge(id, false)
        }
    }

    fun consumeLobbyNavigation() {
        _uiState.update { it.copy(navigateToLobbyRoomId = null) }
    }
}

private fun String.shortId(): String = if (length <= 6) this else substring(0, 6)

private fun parseFriendUidFromQr(rawValue: String): String? {
    val uri = kotlin.runCatching { android.net.Uri.parse(rawValue) }.getOrNull() ?: return null
    if (uri.scheme != "domino-blockade") return null
    if (uri.host != "friend") return null
    return uri.getQueryParameter("uid")
}
