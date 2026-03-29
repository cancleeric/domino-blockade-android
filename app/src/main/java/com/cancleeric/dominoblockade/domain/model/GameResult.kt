package com.cancleeric.dominoblockade.domain.model

/**
 * Summarises the outcome of a completed game round.
 * This is the input consumed by [com.cancleeric.dominoblockade.domain.usecase.CheckAchievementsUseCase]
 * to determine which achievements to unlock.
 *
 * @param isWin              True if the local player won this round.
 * @param isBlocked          True if the game ended in a blockade (draw-pile exhausted, no moves).
 * @param durationSeconds    Wall-clock seconds the round took.
 * @param playerRemainingTiles  How many tiles the winner still held at game end (0 = clean sweep).
 * @param playerRemainingScore  Sum of pips on the winner's remaining tiles.
 * @param totalWins          Cumulative wins of the player, **including** this game.
 * @param consecutiveWins    Unbroken win streak, **including** this game.
 */
data class GameResult(
    val isWin: Boolean,
    val isBlocked: Boolean,
    val durationSeconds: Int,
    val playerRemainingTiles: Int,
    val playerRemainingScore: Int,
    val totalWins: Int,
    val consecutiveWins: Int
)
