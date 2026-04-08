package com.cancleeric.dominoblockade.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cancleeric.dominoblockade.data.local.entity.GameMoveEntity
import com.cancleeric.dominoblockade.data.local.entity.GameReplayEntity

@Dao
interface GameReplayDao {

    @Insert
    suspend fun insertReplay(replay: GameReplayEntity): Long

    @Insert
    suspend fun insertMoves(moves: List<GameMoveEntity>)

    @Query("SELECT * FROM game_replays ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestReplay(): GameReplayEntity?

    @Query("SELECT * FROM game_moves WHERE replayId = :replayId ORDER BY moveIndex ASC")
    suspend fun getMovesForReplay(replayId: Long): List<GameMoveEntity>

    @Query(
        "DELETE FROM game_replays WHERE id NOT IN " +
            "(SELECT id FROM game_replays ORDER BY timestamp DESC LIMIT :keepCount)"
    )
    suspend fun deleteOldReplays(keepCount: Int)

    @Query(
        "DELETE FROM game_moves WHERE replayId NOT IN " +
            "(SELECT id FROM game_replays ORDER BY timestamp DESC LIMIT :keepCount)"
    )
    suspend fun deleteOldMoves(keepCount: Int)
}
