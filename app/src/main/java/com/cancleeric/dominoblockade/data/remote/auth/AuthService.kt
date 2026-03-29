package com.cancleeric.dominoblockade.data.remote.auth

import com.cancleeric.dominoblockade.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Contract for Firebase Authentication operations.
 */
interface AuthService {
    /** Emits the current user, or null when signed out. */
    fun getCurrentUser(): Flow<User?>

    /** Signs in anonymously. Used when a player starts a game without an account. */
    suspend fun signInAnonymously(): User

    /** Signs in with a Google ID token obtained from Google Sign-In. */
    suspend fun signInWithGoogle(idToken: String): User

    /** Signs out the current user. */
    suspend fun signOut()
}
