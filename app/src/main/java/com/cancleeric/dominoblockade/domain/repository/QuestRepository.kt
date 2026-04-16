package com.cancleeric.dominoblockade.domain.repository

import com.cancleeric.dominoblockade.domain.model.QuestDashboard
import com.cancleeric.dominoblockade.domain.model.QuestRewardResult
import kotlinx.coroutines.flow.Flow

interface QuestRepository {
    fun observeDashboard(): Flow<QuestDashboard>
    suspend fun refresh()
    suspend fun recordGameResult(isWin: Boolean, isBlocked: Boolean)
    suspend fun claimReward(taskId: String): QuestRewardResult?
}
