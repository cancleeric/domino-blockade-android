package com.cancleeric.dominoblockade.data.remote.auth

import kotlinx.coroutines.flow.Flow

data class User(
    val uid: String,
    val displayName: String?,
    val isAnonymous: Boolean
)

interface AuthService {
    fun getCurrentUser(): Flow<User?>
    suspend fun signInAnonymously(): User
    suspend fun signInWithGoogle(idToken: String): User
    suspend fun signOut()
}
