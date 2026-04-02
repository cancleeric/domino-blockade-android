package com.cancleeric.dominoblockade.presentation.onlinegame

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameState

internal fun computePlacement(state: GameState, domino: Domino): GameState? =
    if (state.board.isEmpty()) {
        removeFromHand(state, domino)
            .copy(board = listOf(domino), leftEnd = domino.left, rightEnd = domino.right)
    } else {
        placeAtEnd(state, domino, state.rightEnd, isRight = true)
            ?: placeAtEnd(state, domino, state.leftEnd, isRight = false)
    }

@Suppress("ReturnCount")
internal fun placeAtEnd(
    state: GameState,
    domino: Domino,
    endValue: Int?,
    isRight: Boolean
): GameState? {
    val oriented = endValue?.let { orientDomino(domino, it, isRight) } ?: return null
    val base = removeFromHand(state, domino)
    return if (isRight) {
        base.copy(board = base.board + oriented, rightEnd = oriented.right)
    } else {
        base.copy(board = listOf(oriented) + base.board, leftEnd = oriented.left)
    }
}

internal fun orientDomino(domino: Domino, endValue: Int, connectRight: Boolean): Domino? = when {
    connectRight && domino.left == endValue -> domino
    connectRight && domino.right == endValue -> Domino(domino.right, domino.left)
    !connectRight && domino.right == endValue -> domino
    !connectRight && domino.left == endValue -> Domino(domino.right, domino.left)
    else -> null
}

internal fun removeFromHand(state: GameState, domino: Domino): GameState {
    val updated = state.currentPlayer.copy(hand = state.currentPlayer.hand - domino)
    val players = state.players.toMutableList().also { it[state.currentPlayerIndex] = updated }
    return state.copy(players = players)
}

internal fun checkBlocked(state: GameState): Boolean {
    if (state.boneyard.isNotEmpty()) return false
    return state.players.none { player -> player.hand.any { state.canPlace(it) } }
}
