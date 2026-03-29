package com.cancleeric.dominoblockade.domain.model

/**
 * Defines all achievement types in the game.
 * Each entry carries human-readable metadata used for display purposes.
 */
enum class AchievementType(
    val title: String,
    val description: String,
    val badgeEmoji: String
) {
    // ── Win-count milestones ──────────────────────────────────────────────────
    FIRST_WIN(
        title = "首勝",
        description = "贏得你的第一場遊戲",
        badgeEmoji = "🏆"
    ),
    TEN_WINS(
        title = "十勝",
        description = "累積贏得 10 場遊戲",
        badgeEmoji = "🌟"
    ),
    FIFTY_WINS(
        title = "五十勝",
        description = "累積贏得 50 場遊戲",
        badgeEmoji = "💫"
    ),
    HUNDRED_WINS(
        title = "百勝",
        description = "累積贏得 100 場遊戲",
        badgeEmoji = "👑"
    ),

    // ── Consecutive-win streaks ───────────────────────────────────────────────
    WIN_STREAK_3(
        title = "三連勝",
        description = "連續贏得 3 場遊戲",
        badgeEmoji = "🔥"
    ),
    WIN_STREAK_5(
        title = "五連勝",
        description = "連續贏得 5 場遊戲",
        badgeEmoji = "⚡"
    ),
    WIN_STREAK_10(
        title = "十連勝",
        description = "連續贏得 10 場遊戲",
        badgeEmoji = "💥"
    ),

    // ── Special condition wins ────────────────────────────────────────────────
    PERFECT_BLOCK(
        title = "完美封鎖",
        description = "以封鎖方式贏得遊戲",
        badgeEmoji = "🛡️"
    ),
    QUICK_WIN_60(
        title = "速勝",
        description = "在 60 秒內贏得遊戲",
        badgeEmoji = "⏱️"
    ),
    QUICK_WIN_30(
        title = "閃電勝利",
        description = "在 30 秒內贏得遊戲",
        badgeEmoji = "⚡"
    ),
    ZERO_HAND_WIN(
        title = "清空手牌",
        description = "以 0 張骨牌在手中贏得遊戲",
        badgeEmoji = "✨"
    ),
    LOW_SCORE_WIN(
        title = "低分勝利",
        description = "贏得遊戲時剩餘分數不超過 5",
        badgeEmoji = "🎯"
    ),

    // ── Experience milestone ──────────────────────────────────────────────────
    FIRST_BLOCKADE(
        title = "初見封鎖",
        description = "第一次遭遇封鎖局面",
        badgeEmoji = "🚧"
    )
}
