package com.cancleeric.dominoblockade.data.remote.realtime

import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.OnlineRoom
import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus
import com.cancleeric.dominoblockade.domain.repository.OnlineGameRepository
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val PATH_ROOMS = "rooms"
private const val KEY_GUEST_ID = "guestId"
private const val KEY_GUEST_NAME = "guestName"
private const val KEY_STATUS = "status"
private const val KEY_GAME_STATE = "gameState"
private const val ROOM_CODE_LENGTH = 6
private const val ROOM_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

@Singleton
class FirebaseRealtimeGameRepository @Inject constructor(
    private val database: FirebaseDatabase
) : OnlineGameRepository {

    private val roomsRef get() = database.getReference(PATH_ROOMS)

    override suspend fun createRoom(hostId: String, hostName: String): String {
        val roomCode = generateRoomCode()
        val data = mapOf(
            "hostId" to hostId,
            "hostName" to hostName,
            KEY_STATUS to OnlineRoomStatus.WAITING.name
        )
        roomsRef.child(roomCode).setValue(data).await()
        return roomCode
    }

    override suspend fun joinRoom(roomId: String, guestId: String, guestName: String): Boolean {
        val updates: Map<String, Any> = mapOf(
            KEY_GUEST_ID to guestId,
            KEY_GUEST_NAME to guestName,
            KEY_STATUS to OnlineRoomStatus.PLAYING.name
        )
        roomsRef.child(roomId).updateChildren(updates).await()
        return true
    }

    override fun observeRoom(roomId: String): Flow<OnlineRoom> = callbackFlow {
        val ref = roomsRef.child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshotToOnlineRoom(snapshot) ?: return
                trySend(room)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun updateGameState(roomId: String, gameState: GameState) {
        val data = gameStateToMap(gameState)
        roomsRef.child(roomId).child(KEY_GAME_STATE).setValue(data).await()
    }

    override suspend fun leaveRoom(roomId: String) {
        roomsRef.child(roomId).child(KEY_STATUS).setValue(OnlineRoomStatus.FINISHED.name).await()
    }

    override suspend fun registerDisconnectHandler(roomId: String, isHost: Boolean) {
        val key = if (isHost) KEY_HOST_DISCONNECTED_AT else KEY_GUEST_DISCONNECTED_AT
        roomsRef.child(roomId).child(key)
            .onDisconnect()
            .setValue(ServerValue.TIMESTAMP)
            .await()
    }

    override suspend fun markPlayerConnected(roomId: String, isHost: Boolean) {
        val key = if (isHost) KEY_HOST_DISCONNECTED_AT else KEY_GUEST_DISCONNECTED_AT
        roomsRef.child(roomId).child(key).setValue(null).await()
    }

    private fun generateRoomCode(): String =
        (1..ROOM_CODE_LENGTH).map { ROOM_CODE_CHARS.random() }.joinToString("")
}
