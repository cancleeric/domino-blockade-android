package com.cancleeric.dominoblockade.data.model

/**
 * Represents an authenticated user (Firebase Auth).
 */
data class User(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val isAnonymous: Boolean
)
