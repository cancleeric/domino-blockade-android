package com.cancleeric.dominoblockade.presentation.replay

import com.cancleeric.dominoblockade.data.local.entity.GameMoveEntity
import com.cancleeric.dominoblockade.data.local.entity.GameReplayEntity
import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.ReplayStep
import com.cancleeric.dominoblockade.domain.repository.GameReplayRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton helper that records game moves in memory and persists
 * the completed replay to [GameReplayRepository] when a game ends.
 */
@Singleton
class GameReplayRecorder @Inject constructor(
    private val repository: GameReplayRepository
) {
    private val pendingMoves = mutableListOf<GameMoveEntity>()
    private var moveCounter = 0

    fun reset() {
        pendingMoves.clear()
        moveCounter = 0
    }

    fun recordMove(
        playerIndex: Int,
        playerName: String,
        moveType: String,
        domino: Domino?,
        board: List<Domino>,
        boneyardSize: Int
    ) {
        pendingMoves.add(
            GameMoveEntity(
                replayId = 0,
                moveIndex = moveCounter++,
                playerIndex = playerIndex,
                playerName = playerName,
                moveType = moveType,
                dominoLeft = domino?.left ?: NO_PIP,
                dominoRight = domino?.right ?: NO_PIP,
                boardState = ReplayStep.serializeBoard(board),
                boneyardSize = boneyardSize
            )
        )
    }

    suspend fun saveReplay(playerCount: Int, winnerName: String, isBlocked: Boolean) {
        val replay = GameReplayEntity(
            playerCount = playerCount,
            winnerName = winnerName,
            isBlocked = isBlocked
        )
        repository.saveReplay(replay, pendingMoves.toList())
        reset()
    }

    companion object {
        private const val NO_PIP = -1
    }
}
