package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.domain.model.ChallengeInvitation
import com.cancleeric.dominoblockade.domain.model.Friend
import com.cancleeric.dominoblockade.domain.model.FriendRequest
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    suspend fun ensureSignedIn(): String
    suspend fun ensureUserProfile(username: String)
    fun observeMyUsername(): Flow<String>
    fun observeMyQrValue(): Flow<String>
    fun searchPlayers(query: String): Flow<List<Friend>>
    fun observeFriends(): Flow<List<Friend>>
    fun observeIncomingFriendRequests(): Flow<List<FriendRequest>>
    suspend fun sendFriendRequest(targetUid: String)
    suspend fun addFriendByQr(targetUid: String)
    suspend fun respondToFriendRequest(requestId: String, accept: Boolean)
    fun observePendingChallenges(): Flow<List<ChallengeInvitation>>
    suspend fun sendChallenge(friendUid: String, friendName: String, gameMode: String)
    suspend fun respondToChallenge(challengeId: String, accept: Boolean): String?
    suspend fun saveFcmToken(token: String)
    suspend fun updatePresence(isOnline: Boolean)
}
