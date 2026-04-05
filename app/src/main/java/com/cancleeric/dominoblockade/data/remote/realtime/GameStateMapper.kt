package com.cancleeric.dominoblockade.data.remote.realtime

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState
import com.cancleeric.dominoblockade.domain.model.OnlineRoom
import com.cancleeric.dominoblockade.domain.model.OnlineRoomStatus
import com.cancleeric.dominoblockade.domain.model.Player
import com.google.firebase.database.DataSnapshot

private const val KEY_LEFT = "left"
private const val KEY_RIGHT = "right"
private const val KEY_ID = "id"
private const val KEY_NAME = "name"
private const val KEY_HAND = "hand"
private const val KEY_CURRENT_PLAYER_INDEX = "currentPlayerIndex"
private const val KEY_LEFT_END = "leftEnd"
private const val KEY_RIGHT_END = "rightEnd"
private const val KEY_IS_GAME_OVER = "isGameOver"
private const val KEY_IS_BLOCKED = "isBlocked"
private const val KEY_WINNER_NAME = "winnerName"
private const val KEY_BOARD = "board"
private const val KEY_BONEYARD = "boneyard"
private const val KEY_PLAYERS = "players"
private const val KEY_HOST_ID = "hostId"
private const val KEY_HOST_NAME = "hostName"
private const val KEY_GUEST_ID = "guestId"
private const val KEY_GUEST_NAME = "guestName"
private const val KEY_STATUS = "status"
private const val KEY_GAME_STATE = "gameState"
internal const val KEY_HOST_DISCONNECTED_AT = "hostDisconnectedAt"
internal const val KEY_GUEST_DISCONNECTED_AT = "guestDisconnectedAt"

internal fun dominoToMap(domino: Domino): Map<String, Int> =
    mapOf(KEY_LEFT to domino.left, KEY_RIGHT to domino.right)

internal fun mapToDomino(map: Map<*, *>): Domino {
    val left = (map[KEY_LEFT] as? Long)?.toInt() ?: 0
    val right = (map[KEY_RIGHT] as? Long)?.toInt() ?: 0
    return Domino(left, right)
}

internal fun playerToMap(player: Player): Map<String, Any> = mapOf(
    KEY_ID to player.id,
    KEY_NAME to player.name,
    KEY_HAND to player.hand.map { dominoToMap(it) }
)

internal fun mapToPlayer(map: Map<*, *>): Player {
    val id = map[KEY_ID] as? String ?: ""
    val name = map[KEY_NAME] as? String ?: ""
    val handRaw = map[KEY_HAND] as? List<*> ?: emptyList<Any>()
    val hand = handRaw.filterIsInstance<Map<*, *>>().map { mapToDomino(it) }
    return Player(id = id, name = name, hand = hand)
}

internal fun gameStateToMap(state: GameState): Map<String, Any?> = mapOf(
    KEY_CURRENT_PLAYER_INDEX to state.currentPlayerIndex,
    KEY_LEFT_END to state.leftEnd,
    KEY_RIGHT_END to state.rightEnd,
    KEY_IS_GAME_OVER to state.isGameOver,
    KEY_IS_BLOCKED to state.isBlocked,
    KEY_WINNER_NAME to (state.winner?.name ?: ""),
    KEY_BOARD to state.board.map { dominoToMap(it) },
    KEY_BONEYARD to state.boneyard.map { dominoToMap(it) },
    KEY_PLAYERS to state.players.map { playerToMap(it) }
)

@Suppress("ReturnCount")
internal fun snapshotToGameState(snapshot: DataSnapshot): GameState? {
    val map = snapshot.value as? Map<*, *> ?: return null
    val players = (map[KEY_PLAYERS] as? List<*>)
        ?.filterIsInstance<Map<*, *>>()
        ?.map { mapToPlayer(it) }
        ?: return null
    val board = (map[KEY_BOARD] as? List<*>)
        ?.filterIsInstance<Map<*, *>>()
        ?.map { mapToDomino(it) }
        ?: emptyList()
    val boneyard = (map[KEY_BONEYARD] as? List<*>)
        ?.filterIsInstance<Map<*, *>>()
        ?.map { mapToDomino(it) }
        ?: emptyList()
    val currentPlayerIndex = (map[KEY_CURRENT_PLAYER_INDEX] as? Long)?.toInt() ?: 0
    val isGameOver = map[KEY_IS_GAME_OVER] as? Boolean ?: false
    val isBlocked = map[KEY_IS_BLOCKED] as? Boolean ?: false
    val leftEnd = (map[KEY_LEFT_END] as? Long)?.toInt()
    val rightEnd = (map[KEY_RIGHT_END] as? Long)?.toInt()
    val winnerName = map[KEY_WINNER_NAME] as? String
    val winner = if (!winnerName.isNullOrEmpty()) players.find { it.name == winnerName } else null
    return GameState(
        players = players,
        board = board,
        boneyard = boneyard,
        currentPlayerIndex = currentPlayerIndex,
        isGameOver = isGameOver,
        isBlocked = isBlocked,
        leftEnd = leftEnd,
        rightEnd = rightEnd,
        winner = winner
    )
}

@Suppress("ReturnCount")
internal fun snapshotToOnlineRoom(snapshot: DataSnapshot): OnlineRoom? {
    val hostId = snapshot.child(KEY_HOST_ID).getValue(String::class.java) ?: return null
    val hostName = snapshot.child(KEY_HOST_NAME).getValue(String::class.java) ?: return null
    val guestId = snapshot.child(KEY_GUEST_ID).getValue(String::class.java)
    val guestName = snapshot.child(KEY_GUEST_NAME).getValue(String::class.java)
    val statusStr = snapshot.child(KEY_STATUS).getValue(String::class.java)
    val status = statusStr?.let {
        runCatching { OnlineRoomStatus.valueOf(it) }.getOrNull()
    } ?: OnlineRoomStatus.WAITING
    val gameState = if (snapshot.hasChild(KEY_GAME_STATE)) {
        snapshotToGameState(snapshot.child(KEY_GAME_STATE))
    } else {
        null
    }
    return OnlineRoom(
        roomId = snapshot.key ?: "",
        hostId = hostId,
        hostName = hostName,
        guestId = guestId,
        guestName = guestName,
        status = status,
        gameState = gameState,
        hostDisconnectedAt = snapshot.child(KEY_HOST_DISCONNECTED_AT).getValue(Long::class.java),
        guestDisconnectedAt = snapshot.child(KEY_GUEST_DISCONNECTED_AT).getValue(Long::class.java)
    )
}
