package com.cancleeric.dominoblockade.domain.usecase

import com.cancleeric.dominoblockade.domain.model.AchievementType
import com.cancleeric.dominoblockade.domain.model.GameResult
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckAchievementsUseCaseTest {

    private lateinit var repository: AchievementRepository
    private lateinit var useCase: CheckAchievementsUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = CheckAchievementsUseCase(repository)
        // By default, no achievement is unlocked
        coEvery { repository.isUnlocked(any()) } returns false
    }

    private fun gameResult(
        isWin: Boolean = true,
        isBlocked: Boolean = false,
        durationSeconds: Int = 120,
        playerRemainingTiles: Int = 0,
        playerRemainingScore: Int = 0,
        totalWins: Int = 1,
        consecutiveWins: Int = 1
    ) = GameResult(
        isWin = isWin,
        isBlocked = isBlocked,
        durationSeconds = durationSeconds,
        playerRemainingTiles = playerRemainingTiles,
        playerRemainingScore = playerRemainingScore,
        totalWins = totalWins,
        consecutiveWins = consecutiveWins
    )

    // ── FIRST_WIN ─────────────────────────────────────────────────────────────

    @Test
    fun `FIRST_WIN unlocked on first game win`() = runTest {
        val result = useCase(gameResult(isWin = true, totalWins = 1))

        val types = result.map { it.type }
        assertTrue(AchievementType.FIRST_WIN in types)
        coVerify { repository.unlockAchievement(AchievementType.FIRST_WIN) }
    }

    @Test
    fun `FIRST_WIN not unlocked on loss`() = runTest {
        val result = useCase(gameResult(isWin = false, totalWins = 0))

        val types = result.map { it.type }
        assertTrue(AchievementType.FIRST_WIN !in types)
    }

    // ── TEN_WINS ──────────────────────────────────────────────────────────────

    @Test
    fun `TEN_WINS unlocked when totalWins reaches 10`() = runTest {
        val result = useCase(gameResult(isWin = true, totalWins = 10))

        val types = result.map { it.type }
        assertTrue(AchievementType.TEN_WINS in types)
    }

    @Test
    fun `TEN_WINS not unlocked when totalWins is 9`() = runTest {
        val result = useCase(gameResult(isWin = true, totalWins = 9))

        val types = result.map { it.type }
        assertTrue(AchievementType.TEN_WINS !in types)
    }

    // ── FIFTY_WINS ────────────────────────────────────────────────────────────

    @Test
    fun `FIFTY_WINS unlocked when totalWins reaches 50`() = runTest {
        val result = useCase(gameResult(isWin = true, totalWins = 50))

        val types = result.map { it.type }
        assertTrue(AchievementType.FIFTY_WINS in types)
    }

    // ── HUNDRED_WINS ──────────────────────────────────────────────────────────

    @Test
    fun `HUNDRED_WINS unlocked when totalWins reaches 100`() = runTest {
        val result = useCase(gameResult(isWin = true, totalWins = 100))

        val types = result.map { it.type }
        assertTrue(AchievementType.HUNDRED_WINS in types)
    }

    // ── WIN_STREAK_3 ──────────────────────────────────────────────────────────

    @Test
    fun `WIN_STREAK_3 unlocked when consecutiveWins reaches 3`() = runTest {
        val result = useCase(gameResult(isWin = true, consecutiveWins = 3))

        val types = result.map { it.type }
        assertTrue(AchievementType.WIN_STREAK_3 in types)
    }

    @Test
    fun `WIN_STREAK_3 not unlocked when consecutiveWins is 2`() = runTest {
        val result = useCase(gameResult(isWin = true, consecutiveWins = 2))

        val types = result.map { it.type }
        assertTrue(AchievementType.WIN_STREAK_3 !in types)
    }

    // ── WIN_STREAK_5 ──────────────────────────────────────────────────────────

    @Test
    fun `WIN_STREAK_5 unlocked when consecutiveWins reaches 5`() = runTest {
        val result = useCase(gameResult(isWin = true, consecutiveWins = 5))

        val types = result.map { it.type }
        assertTrue(AchievementType.WIN_STREAK_5 in types)
    }

    // ── WIN_STREAK_10 ─────────────────────────────────────────────────────────

    @Test
    fun `WIN_STREAK_10 unlocked when consecutiveWins reaches 10`() = runTest {
        val result = useCase(gameResult(isWin = true, consecutiveWins = 10))

        val types = result.map { it.type }
        assertTrue(AchievementType.WIN_STREAK_10 in types)
    }

    // ── PERFECT_BLOCK ─────────────────────────────────────────────────────────

    @Test
    fun `PERFECT_BLOCK unlocked when win via blockade`() = runTest {
        val result = useCase(gameResult(isWin = true, isBlocked = true))

        val types = result.map { it.type }
        assertTrue(AchievementType.PERFECT_BLOCK in types)
    }

    @Test
    fun `PERFECT_BLOCK not unlocked when blockade but loss`() = runTest {
        val result = useCase(gameResult(isWin = false, isBlocked = true))

        val types = result.map { it.type }
        assertTrue(AchievementType.PERFECT_BLOCK !in types)
    }

    // ── QUICK_WIN_60 ──────────────────────────────────────────────────────────

    @Test
    fun `QUICK_WIN_60 unlocked when win in under 60 seconds`() = runTest {
        val result = useCase(gameResult(isWin = true, durationSeconds = 45))

        val types = result.map { it.type }
        assertTrue(AchievementType.QUICK_WIN_60 in types)
    }

    @Test
    fun `QUICK_WIN_60 not unlocked when win in exactly 60 seconds`() = runTest {
        val result = useCase(gameResult(isWin = true, durationSeconds = 60))

        val types = result.map { it.type }
        assertTrue(AchievementType.QUICK_WIN_60 !in types)
    }

    // ── QUICK_WIN_30 ──────────────────────────────────────────────────────────

    @Test
    fun `QUICK_WIN_30 unlocked when win in under 30 seconds`() = runTest {
        val result = useCase(gameResult(isWin = true, durationSeconds = 15))

        val types = result.map { it.type }
        assertTrue(AchievementType.QUICK_WIN_30 in types)
        // Also QUICK_WIN_60 should be unlocked
        assertTrue(AchievementType.QUICK_WIN_60 in types)
    }

    // ── ZERO_HAND_WIN ─────────────────────────────────────────────────────────

    @Test
    fun `ZERO_HAND_WIN unlocked when win with 0 remaining tiles`() = runTest {
        val result = useCase(gameResult(isWin = true, playerRemainingTiles = 0))

        val types = result.map { it.type }
        assertTrue(AchievementType.ZERO_HAND_WIN in types)
    }

    @Test
    fun `ZERO_HAND_WIN not unlocked when remaining tiles greater than 0`() = runTest {
        val result = useCase(gameResult(isWin = true, playerRemainingTiles = 2))

        val types = result.map { it.type }
        assertTrue(AchievementType.ZERO_HAND_WIN !in types)
    }

    // ── LOW_SCORE_WIN ─────────────────────────────────────────────────────────

    @Test
    fun `LOW_SCORE_WIN unlocked when win with score of 5 or less`() = runTest {
        val result = useCase(gameResult(isWin = true, playerRemainingScore = 5))

        val types = result.map { it.type }
        assertTrue(AchievementType.LOW_SCORE_WIN in types)
    }

    @Test
    fun `LOW_SCORE_WIN not unlocked when remaining score is 6`() = runTest {
        val result = useCase(gameResult(isWin = true, playerRemainingScore = 6))

        val types = result.map { it.type }
        assertTrue(AchievementType.LOW_SCORE_WIN !in types)
    }

    // ── FIRST_BLOCKADE ────────────────────────────────────────────────────────

    @Test
    fun `FIRST_BLOCKADE unlocked on any blocked game regardless of win status`() = runTest {
        val result = useCase(gameResult(isWin = false, isBlocked = true))

        val types = result.map { it.type }
        assertTrue(AchievementType.FIRST_BLOCKADE in types)
    }

    @Test
    fun `FIRST_BLOCKADE not unlocked when game is not blocked`() = runTest {
        val result = useCase(gameResult(isWin = true, isBlocked = false))

        val types = result.map { it.type }
        assertTrue(AchievementType.FIRST_BLOCKADE !in types)
    }

    // ── Already unlocked ──────────────────────────────────────────────────────

    @Test
    fun `already unlocked achievements are not returned again`() = runTest {
        coEvery { repository.isUnlocked(AchievementType.FIRST_WIN) } returns true

        val result = useCase(gameResult(isWin = true, totalWins = 1))

        val types = result.map { it.type }
        assertTrue(AchievementType.FIRST_WIN !in types)
        coVerify(exactly = 0) { repository.unlockAchievement(AchievementType.FIRST_WIN) }
    }

    // ── Multiple unlocks in one game ──────────────────────────────────────────

    @Test
    fun `multiple achievements can be unlocked in a single game`() = runTest {
        val result = useCase(
            gameResult(
                isWin = true,
                totalWins = 10,
                consecutiveWins = 3,
                durationSeconds = 45,
                isBlocked = false
            )
        )

        val types = result.map { it.type }
        assertTrue(AchievementType.FIRST_WIN in types)
        assertTrue(AchievementType.TEN_WINS in types)
        assertTrue(AchievementType.WIN_STREAK_3 in types)
        assertTrue(AchievementType.QUICK_WIN_60 in types)
    }

    // ── No achievements on loss without special conditions ────────────────────

    @Test
    fun `loss with no special conditions unlocks no achievements`() = runTest {
        val result = useCase(
            gameResult(
                isWin = false,
                isBlocked = false,
                durationSeconds = 200,
                playerRemainingTiles = 3,
                playerRemainingScore = 20,
                totalWins = 0,
                consecutiveWins = 0
            )
        )

        assertTrue(result.isEmpty())
    }
}
