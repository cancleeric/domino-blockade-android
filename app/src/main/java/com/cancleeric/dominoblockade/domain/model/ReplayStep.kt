package com.cancleeric.dominoblockade.domain.model

/**
 * Represents a single step in a game replay, containing the board state
 * and move information for that step.
 *
 * @property moveIndex Zero-based index of this move in the game.
 * @property playerName Name of the player who made this move.
 * @property moveType Type of move: "DEAL", "PLACE", "DRAW", or "SKIP".
 * @property dominoLeft Left pip value of the placed domino (-1 if not a place move).
 * @property dominoRight Right pip value of the placed domino (-1 if not a place move).
 * @property board Board state after this move.
 * @property boneyardSize Number of tiles remaining in the boneyard after this move.
 */
data class ReplayStep(
    val moveIndex: Int,
    val playerName: String,
    val moveType: String,
    val dominoLeft: Int,
    val dominoRight: Int,
    val board: List<Domino>,
    val boneyardSize: Int
) {
    /** The placed domino, or null if this was not a PLACE move. */
    val placedDomino: Domino?
        get() = if (dominoLeft >= 0) Domino(dominoLeft, dominoRight) else null

    /** Human-readable description of what happened in this step. */
    val moveDescription: String
        get() = when (moveType) {
            MOVE_DEAL -> "Game started — tiles dealt"
            MOVE_PLACE -> "$playerName placed [$dominoLeft|$dominoRight]"
            MOVE_DRAW -> "$playerName drew a tile"
            MOVE_SKIP -> "$playerName skipped (no moves)"
            else -> moveType
        }

    companion object {
        const val MOVE_DEAL = "DEAL"
        const val MOVE_PLACE = "PLACE"
        const val MOVE_DRAW = "DRAW"
        const val MOVE_SKIP = "SKIP"

        /** Serializes a board to a compact string: e.g. "3|4,4|2,2|6" */
        fun serializeBoard(board: List<Domino>): String =
            board.joinToString(",") { "${it.left}|${it.right}" }

        /** Deserializes a board string back to a list of Domino objects. */
        fun deserializeBoard(s: String): List<Domino> =
            if (s.isEmpty()) {
                emptyList()
            } else {
                s.split(",").map { pair ->
                    val parts = pair.split("|")
                    Domino(parts[0].toInt(), parts[1].toInt())
                }
            }
    }
}
