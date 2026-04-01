package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.Achievement
import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.domain.model.GameResult
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckAchievementsUseCaseTest {

    private class FakeAchievementRepository : AchievementRepository {
        private val unlocked = mutableSetOf<AchievementType>()
        private val allFlow = MutableStateFlow<List<Achievement>>(emptyList())

        override fun getAll(): Flow<List<Achievement>> = allFlow

        override suspend fun getAllUnlocked(): List<Achievement> =
            unlocked.map { Achievement(type = it, isUnlocked = true, unlockedAt = 0L) }

        override suspend fun unlock(type: AchievementType) {
            unlocked.add(type)
        }

        override suspend fun isUnlocked(type: AchievementType): Boolean = type in unlocked
    }

    private val repository = FakeAchievementRepository()
    private val useCase = CheckAchievementsUseCase(repository)

    @Test
    fun `first win unlocks FIRST_WIN achievement`() = runTest {
        val result = GameResult(isWin = true, isBlocked = false, totalWins = 1, totalGames = 1)
        val unlocked = useCase(result)
        assertTrue(unlocked.contains(AchievementType.FIRST_WIN))
    }

    @Test
    fun `loss does not unlock FIRST_WIN`() = runTest {
        val result = GameResult(isWin = false, isBlocked = false, totalWins = 0, totalGames = 1)
        val unlocked = useCase(result)
        assertTrue(unlocked.none { it == AchievementType.FIRST_WIN })
    }

    @Test
    fun `blocked game unlocks BLOCKADE_PLAYED`() = runTest {
        val result = GameResult(isWin = false, isBlocked = true, totalWins = 0, totalGames = 1)
        val unlocked = useCase(result)
        assertTrue(unlocked.contains(AchievementType.BLOCKADE_PLAYED))
    }

    @Test
    fun `already unlocked achievement is not returned again`() = runTest {
        val result = GameResult(isWin = true, isBlocked = false, totalWins = 1, totalGames = 1)
        useCase(result)
        val second = useCase(result)
        assertTrue(second.none { it == AchievementType.FIRST_WIN })
    }

    @Test
    fun `three wins unlocks WIN_STREAK_3`() = runTest {
        val result = GameResult(isWin = true, isBlocked = false, totalWins = 3, totalGames = 3)
        val unlocked = useCase(result)
        assertTrue(unlocked.contains(AchievementType.WIN_STREAK_3))
    }

    @Test
    fun `twenty games unlocks VETERAN`() = runTest {
        val result = GameResult(isWin = false, isBlocked = false, totalWins = 0, totalGames = 20)
        val unlocked = useCase(result)
        assertTrue(unlocked.contains(AchievementType.VETERAN))
    }

    @Test
    fun `returns empty list when no new achievements`() = runTest {
        val result = GameResult(isWin = false, isBlocked = false, totalWins = 0, totalGames = 1)
        val unlocked = useCase(result)
        assertEquals(0, unlocked.size)
    }
}
