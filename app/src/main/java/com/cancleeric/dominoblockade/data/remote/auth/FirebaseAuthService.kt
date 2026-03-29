package com.cancleeric.dominoblockade.data.remote.auth

import com.cancleeric.dominoblockade.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase implementation of [AuthService].
 *
 * - Anonymous sign-in: allows players to use the game without registering.
 * - Google Sign-In: links an anonymous account to a Google identity for
 *   cross-platform leaderboard access shared with iOS LifeSnap.
 */
class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthService {

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toDomainUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInAnonymously(): User {
        val result = firebaseAuth.signInAnonymously().await()
        return requireNotNull(result.user?.toDomainUser()) {
            "Anonymous sign-in succeeded but user is null"
        }
    }

    override suspend fun signInWithGoogle(idToken: String): User {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val currentUser = firebaseAuth.currentUser
        val result = if (currentUser != null && currentUser.isAnonymous) {
            // Link anonymous account with Google credentials so the player
            // keeps any progress made while playing anonymously.
            currentUser.linkWithCredential(credential).await()
        } else {
            firebaseAuth.signInWithCredential(credential).await()
        }
        return requireNotNull(result.user?.toDomainUser()) {
            "Google sign-in succeeded but user is null"
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private fun FirebaseUser.toDomainUser() = User(
        uid = uid,
        displayName = displayName,
        email = email,
        isAnonymous = isAnonymous
    )
}
