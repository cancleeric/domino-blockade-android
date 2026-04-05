package com.cancleeric.dominoblockade.data.local

import com.cancleeric.dominoblockade.domain.model.Domino
import com.cancleeric.dominoblockade.domain.model.GameMove
import com.cancleeric.dominoblockade.domain.model.Player

/**
 * Serializes and deserializes [GameMove] lists and [Player] lists (with hands)
 * to/from JSON strings for storage in Room. Uses only Kotlin stdlib — no external
 * JSON library — so it is safe to use in both Android and pure-JVM unit tests.
 */
object MoveHistorySerializer {

    // ── Public API ──────────────────────────────────────────────────────────────

    fun serializeMoves(moves: List<GameMove>): String {
        val sb = StringBuilder("[")
        moves.forEachIndexed { index, move ->
            if (index > 0) sb.append(",")
            sb.append("{")
            sb.append("\"playerId\":\"${esc(move.playerId)}\",")
            sb.append("\"playerName\":\"${esc(move.playerName)}\",")
            sb.append("\"dominoLeft\":${move.dominoLeft},")
            sb.append("\"dominoRight\":${move.dominoRight},")
            sb.append("\"boardEnd\":\"${move.boardEnd}\",")
            sb.append("\"timestamp\":${move.timestamp}")
            sb.append("}")
        }
        sb.append("]")
        return sb.toString()
    }

    fun deserializeMoves(json: String): List<GameMove> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return emptyList()
        return splitTopLevelObjects(trimmed.removePrefix("[").removeSuffix("]"))
            .mapNotNull { parseMove(it) }
    }

    fun serializePlayers(players: List<Player>): String {
        val sb = StringBuilder("[")
        players.forEachIndexed { pIdx, player ->
            if (pIdx > 0) sb.append(",")
            sb.append("{")
            sb.append("\"id\":\"${esc(player.id)}\",")
            sb.append("\"name\":\"${esc(player.name)}\",")
            sb.append("\"hand\":[")
            player.hand.forEachIndexed { dIdx, domino ->
                if (dIdx > 0) sb.append(",")
                sb.append("{\"left\":${domino.left},\"right\":${domino.right}}")
            }
            sb.append("]}")
        }
        sb.append("]")
        return sb.toString()
    }

    fun deserializePlayers(json: String): List<Player> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return emptyList()
        return splitTopLevelObjects(trimmed.removePrefix("[").removeSuffix("]"))
            .mapNotNull { parsePlayer(it) }
    }

    // ── Serialization helpers ────────────────────────────────────────────────────

    private fun esc(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"")

    // ── Splitting ────────────────────────────────────────────────────────────────

    /**
     * Splits a JSON string (the inner content of a top-level array, without
     * the surrounding `[` and `]`) into individual top-level `{...}` objects.
     */
    private fun splitTopLevelObjects(inner: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        var start = -1
        for (i in inner.indices) {
            when (inner[i]) {
                '{' -> {
                    if (depth == 0) start = i
                    depth++
                }
                '}' -> {
                    depth--
                    if (depth == 0 && start >= 0) {
                        result.add(inner.substring(start, i + 1))
                        start = -1
                    }
                }
            }
        }
        return result
    }

    // ── Field readers ────────────────────────────────────────────────────────────

    private fun readStringField(json: String, key: String): String? =
        Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
            .find(json)?.groupValues?.get(1)
            ?.replace("\\\"", "\"")
            ?.replace("\\\\", "\\")

    private fun readIntField(json: String, key: String): Int? =
        Regex("\"${Regex.escape(key)}\"\\s*:\\s*(-?\\d+)")
            .find(json)?.groupValues?.get(1)?.toIntOrNull()

    private fun readLongField(json: String, key: String): Long? =
        Regex("\"${Regex.escape(key)}\"\\s*:\\s*(-?\\d+)")
            .find(json)?.groupValues?.get(1)?.toLongOrNull()

    /** Extracts the raw JSON array string for a given key inside an object JSON string. */
    private fun readArrayField(json: String, key: String): String? {
        val marker = "\"${key}\":["
        val startIdx = json.indexOf(marker).takeIf { it >= 0 } ?: return null
        val arrayStart = startIdx + marker.length - 1
        var depth = 0
        for (i in arrayStart until json.length) {
            when (json[i]) {
                '[' -> depth++
                ']' -> {
                    depth--
                    if (depth == 0) return json.substring(arrayStart, i + 1)
                }
            }
        }
        return null
    }

    // ── Parsers ──────────────────────────────────────────────────────────────────

    private fun parseMove(obj: String): GameMove? = try {
        GameMove(
            playerId = readStringField(obj, "playerId") ?: return null,
            playerName = readStringField(obj, "playerName") ?: return null,
            dominoLeft = readIntField(obj, "dominoLeft") ?: return null,
            dominoRight = readIntField(obj, "dominoRight") ?: return null,
            boardEnd = readStringField(obj, "boardEnd") ?: return null,
            timestamp = readLongField(obj, "timestamp") ?: return null
        )
    } catch (_: Exception) {
        null
    }

    private fun parsePlayer(obj: String): Player? = try {
        val id = readStringField(obj, "id") ?: return null
        val name = readStringField(obj, "name") ?: return null
        val handJson = readArrayField(obj, "hand") ?: "[]"
        val hand = parseHand(handJson)
        Player(id = id, name = name, hand = hand)
    } catch (_: Exception) {
        null
    }

    private fun parseHand(json: String): List<Domino> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return emptyList()
        return splitTopLevelObjects(trimmed.removePrefix("[").removeSuffix("]"))
            .mapNotNull { obj ->
                val left = readIntField(obj, "left") ?: return@mapNotNull null
                val right = readIntField(obj, "right") ?: return@mapNotNull null
                Domino(left, right)
            }
    }
}
