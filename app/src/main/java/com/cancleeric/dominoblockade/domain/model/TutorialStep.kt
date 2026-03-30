package com.cancleeric.dominoblockade.domain.model

data class TutorialStep(
    val stepIndex: Int,
    val titleRes: Int,
    val messageRes: Int,
    val highlightTarget: HighlightTarget
)

enum class HighlightTarget {
    NONE,
    BOARD,
    PLAYER_HAND,
    BONEYARD,
    SCORE
}
