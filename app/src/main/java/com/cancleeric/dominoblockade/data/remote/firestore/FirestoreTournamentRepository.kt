package com.cancleeric.dominoblockade.data.remote.firestore

import com.cancleeric.dominoblockade.domain.model.Tournament
import com.cancleeric.dominoblockade.domain.model.TournamentMatch
import com.cancleeric.dominoblockade.domain.model.TournamentPlayer
import com.cancleeric.dominoblockade.domain.model.TournamentStatus
import com.cancleeric.dominoblockade.domain.repository.TournamentRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val COLLECTION_TOURNAMENTS = "tournaments"
private const val FIELD_PLAYER_COUNT = "playerCount"
private const val FIELD_STATUS = "status"
private const val FIELD_CREATED_AT = "createdAt"
private const val FIELD_ROUNDS = "rounds"
private const val FIELD_MATCH_ID = "matchId"
private const val FIELD_PLAYER1 = "player1"
private const val FIELD_PLAYER2 = "player2"
private const val FIELD_WINNER_ID = "winnerId"
private const val FIELD_ROUND_NUMBER = "roundNumber"
private const val FIELD_MATCH_INDEX = "matchIndex"
private const val FIELD_PLAYER_ID = "playerId"
private const val FIELD_PLAYER_NAME = "playerName"
private const val FIELD_SCORE = "score"
private const val QUERY_LIMIT = 1L
private const val NEXT_MATCH_DIVISOR = 2

@Singleton
class FirestoreTournamentRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : TournamentRepository {

    private val collectionRef get() = firestore.collection(COLLECTION_TOURNAMENTS)

    override fun getActiveTournament(): Flow<Tournament?> = callbackFlow {
        val registration = collectionRef
            .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .limit(QUERY_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val doc = snapshot?.documents?.firstOrNull()
                trySend(doc?.toTournament())
            }
        awaitClose { registration.remove() }
    }

    override suspend fun createTournament(tournament: Tournament) {
        val data = tournament.toMap()
        collectionRef.document(tournament.id).set(data).await()
    }

    override suspend fun updateMatch(tournamentId: String, roundIndex: Int, matchIndex: Int, winnerId: String) {
        val docRef = collectionRef.document(tournamentId)
        val snapshot = docRef.get().await()
        val tournament = snapshot.toTournament() ?: return
        val updatedRounds = applyMatchUpdate(tournament, roundIndex, matchIndex, winnerId)
        val lastRound = updatedRounds.lastOrNull()
        val isCompleted = lastRound?.lastOrNull()?.winnerId != null
        val newStatus = if (isCompleted) TournamentStatus.COMPLETED else tournament.status
        val updated = tournament.copy(rounds = updatedRounds, status = newStatus)
        docRef.set(updated.toMap()).await()
    }

    override suspend fun deleteTournament(tournamentId: String) {
        collectionRef.document(tournamentId).delete().await()
    }

    private fun applyMatchUpdate(
        tournament: Tournament,
        roundIndex: Int,
        matchIndex: Int,
        winnerId: String
    ): List<List<TournamentMatch>> {
        val mutableRounds = tournament.rounds.map { it.toMutableList() }.toMutableList()
        val match = mutableRounds.getOrNull(roundIndex)?.getOrNull(matchIndex) ?: return tournament.rounds
        val winner = if (match.player1?.playerId == winnerId) match.player1 else match.player2
        mutableRounds[roundIndex][matchIndex] = match.copy(winnerId = winnerId)
        val nextRoundIndex = roundIndex + 1
        if (winner != null && nextRoundIndex < mutableRounds.size) {
            val nextMatchIndex = matchIndex / NEXT_MATCH_DIVISOR
            val nextMatch = mutableRounds[nextRoundIndex].getOrNull(nextMatchIndex) ?: return mutableRounds
            val updatedNext = if (matchIndex % NEXT_MATCH_DIVISOR == 0) {
                nextMatch.copy(player1 = winner)
            } else {
                nextMatch.copy(player2 = winner)
            }
            mutableRounds[nextRoundIndex][nextMatchIndex] = updatedNext
        }
        return mutableRounds
    }

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toTournament(): Tournament? {
        val playerCount = getLong(FIELD_PLAYER_COUNT)?.toInt() ?: return null
        val statusStr = getString(FIELD_STATUS) ?: return null
        val status = runCatching { TournamentStatus.valueOf(statusStr) }.getOrNull() ?: return null
        val createdAt = getLong(FIELD_CREATED_AT) ?: 0L
        val rawRounds = get(FIELD_ROUNDS) as? List<List<Map<String, Any?>>> ?: emptyList()
        val rounds = rawRounds.map { rawRound -> rawRound.map { it.toTournamentMatch() } }
        return Tournament(id = id, playerCount = playerCount, status = status, rounds = rounds, createdAt = createdAt)
    }

    private fun Map<String, Any?>.toTournamentMatch(): TournamentMatch {
        return TournamentMatch(
            matchId = get(FIELD_MATCH_ID) as? String ?: "",
            player1 = (get(FIELD_PLAYER1) as? Map<String, Any?>)?.toTournamentPlayer(),
            player2 = (get(FIELD_PLAYER2) as? Map<String, Any?>)?.toTournamentPlayer(),
            winnerId = get(FIELD_WINNER_ID) as? String,
            roundNumber = (get(FIELD_ROUND_NUMBER) as? Long)?.toInt() ?: 0,
            matchIndex = (get(FIELD_MATCH_INDEX) as? Long)?.toInt() ?: 0
        )
    }

    private fun Map<String, Any?>.toTournamentPlayer(): TournamentPlayer {
        return TournamentPlayer(
            playerId = get(FIELD_PLAYER_ID) as? String ?: "",
            playerName = get(FIELD_PLAYER_NAME) as? String ?: "",
            score = (get(FIELD_SCORE) as? Long)?.toInt() ?: 0
        )
    }

    private fun Tournament.toMap(): Map<String, Any?> = mapOf(
        FIELD_PLAYER_COUNT to playerCount,
        FIELD_STATUS to status.name,
        FIELD_CREATED_AT to createdAt,
        FIELD_ROUNDS to rounds.map { round -> round.map { it.toMap() } }
    )

    private fun TournamentMatch.toMap(): Map<String, Any?> = mapOf(
        FIELD_MATCH_ID to matchId,
        FIELD_PLAYER1 to player1?.toMap(),
        FIELD_PLAYER2 to player2?.toMap(),
        FIELD_WINNER_ID to winnerId,
        FIELD_ROUND_NUMBER to roundNumber,
        FIELD_MATCH_INDEX to matchIndex
    )

    private fun TournamentPlayer.toMap(): Map<String, Any?> = mapOf(
        FIELD_PLAYER_ID to playerId,
        FIELD_PLAYER_NAME to playerName,
        FIELD_SCORE to score
    )
}
