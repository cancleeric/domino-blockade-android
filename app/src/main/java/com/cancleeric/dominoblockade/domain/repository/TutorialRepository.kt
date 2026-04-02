package com.cancleeric.dominoblockade.domain.repository

import kotlinx.coroutines.flow.Flow

interface TutorialRepository {
    val isTutorialCompleted: Flow<Boolean>
    suspend fun markTutorialCompleted()
}
