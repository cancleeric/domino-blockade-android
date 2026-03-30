package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.GameRecord
import com.cancleeric.dominoblockade.data.local.GameRecordDao
import com.cancleeric.dominoblockade.domain.model.DifficultyStats
import com.cancleeric.dominoblockade.domain.model.GameStats
import com.cancleeric.dominoblockade.domain.repository.GameHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameHistoryRepositoryImpl @Inject constructor(
    private val dao: GameRecordDao
) : GameHistoryRepository {

    override fun getAllRecords(): Flow<List<GameRecord>> = dao.getAllRecords()

    override fun getRecordById(id: Long): Flow<GameRecord?> = dao.getRecordById(id)

    override fun getStats(): Flow<GameStats> = dao.getAllRecords().map { records ->
        if (records.isEmpty()) {
            GameStats()
        } else {
            val totalWins = records.count { it.isWin }
            val winRate = totalWins.toFloat() / records.size
            val bestScore = records.maxOfOrNull { it.playerScore } ?: 0
            val consecutiveWins = calculateConsecutiveWins(records)

            GameStats(
                totalGames = records.size,
                totalWins = totalWins,
                winRate = winRate,
                bestScore = bestScore,
                consecutiveWins = consecutiveWins,
                easyStats = buildDifficultyStats(records, "EASY"),
                mediumStats = buildDifficultyStats(records, "MEDIUM"),
                hardStats = buildDifficultyStats(records, "HARD")
            )
        }
    }

    override suspend fun saveRecord(record: GameRecord) = dao.insertRecord(record)

    override suspend fun clearAllRecords() = dao.deleteAllRecords()

    private fun buildDifficultyStats(records: List<GameRecord>, difficulty: String): DifficultyStats {
        val filtered = records.filter { it.difficulty == difficulty }
        if (filtered.isEmpty()) return DifficultyStats()
        val wins = filtered.count { it.isWin }
        return DifficultyStats(
            totalGames = filtered.size,
            totalWins = wins,
            winRate = wins.toFloat() / filtered.size,
            bestScore = filtered.maxOfOrNull { it.playerScore } ?: 0
        )
    }

    private fun calculateConsecutiveWins(records: List<GameRecord>): Int {
        var consecutive = 0
        for (record in records) {
            if (record.isWin) consecutive++ else break
        }
        return consecutive
    }
}
