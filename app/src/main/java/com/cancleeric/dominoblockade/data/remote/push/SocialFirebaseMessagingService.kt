package com.cancleeric.dominoblockade.data.remote.push

import com.cancleeric.dominoblockade.domain.repository.SocialRepository
import com.cancleeric.dominoblockade.presentation.notification.SocialNotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_TYPE = "type"
private const val KEY_CHALLENGE_ID = "challengeId"
private const val KEY_CHALLENGER_NAME = "challengerName"
private const val KEY_GAME_MODE = "gameMode"
private const val KEY_ACCEPT_DEEP_LINK = "acceptDeepLink"
private const val KEY_DECLINE_DEEP_LINK = "declineDeepLink"
private const val TYPE_CHALLENGE_INVITATION = "challenge_invitation"

@AndroidEntryPoint
class SocialFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var socialRepository: SocialRepository

    @Inject
    lateinit var notificationHelper: SocialNotificationHelper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data[KEY_TYPE] != TYPE_CHALLENGE_INVITATION) return
        val challengeId = data[KEY_CHALLENGE_ID].orEmpty()
        val challengerName = data[KEY_CHALLENGER_NAME].orEmpty()
        val gameMode = data[KEY_GAME_MODE].orEmpty().ifBlank { "Classic" }
        val acceptDeepLink = data[KEY_ACCEPT_DEEP_LINK].orEmpty()
        val declineDeepLink = data[KEY_DECLINE_DEEP_LINK].orEmpty()
        if (challengeId.isBlank() || acceptDeepLink.isBlank() || declineDeepLink.isBlank()) return
        notificationHelper.showChallengeNotification(
            challengeId = challengeId,
            challengerName = challengerName.ifBlank { "Friend" },
            gameMode = gameMode,
            acceptDeepLink = acceptDeepLink,
            declineDeepLink = declineDeepLink
        )
    }

    override fun onNewToken(token: String) {
        if (token.isBlank()) return
        serviceScope.launch {
            runCatching { socialRepository.saveFcmToken(token) }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
