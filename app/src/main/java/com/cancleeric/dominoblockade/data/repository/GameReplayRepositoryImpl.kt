package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.GameReplayDao
import com.cancleeric.dominoblockade.data.local.entity.GameMoveEntity
import com.cancleeric.dominoblockade.data.local.entity.GameReplayEntity
import com.cancleeric.dominoblockade.domain.repository.GameReplayRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameReplayRepositoryImpl @Inject constructor(
    private val dao: GameReplayDao
) : GameReplayRepository {

    override suspend fun saveReplay(replay: GameReplayEntity, moves: List<GameMoveEntity>) {
        val replayId = dao.insertReplay(replay)
        val movesWithId = moves.map { it.copy(replayId = replayId) }
        dao.insertMoves(movesWithId)
        dao.deleteOldReplays(MAX_REPLAYS)
        dao.deleteOldMoves(MAX_REPLAYS)
    }

    override suspend fun getLatestReplayWithMoves(): Pair<GameReplayEntity, List<GameMoveEntity>>? {
        val replay = dao.getLatestReplay() ?: return null
        val moves = dao.getMovesForReplay(replay.id)
        return replay to moves
    }

    companion object {
        private const val MAX_REPLAYS = 5
    }
}
