package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.data.local.entity.GameMoveEntity
import com.cancleeric.dominoblockade.data.local.entity.GameReplayEntity

interface GameReplayRepository {
    suspend fun saveReplay(replay: GameReplayEntity, moves: List<GameMoveEntity>)
    suspend fun getLatestReplayWithMoves(): Pair<GameReplayEntity, List<GameMoveEntity>>?
}
