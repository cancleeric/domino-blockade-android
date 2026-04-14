package com.cancleeric.dominoblockade.domain.repository

import kotlinx.coroutines.flow.Flow

sealed interface PurchaseResult {
    data object Success : PurchaseResult
    data object NotFound : PurchaseResult
    data object AlreadyOwned : PurchaseResult
    data object InsufficientCoins : PurchaseResult
    data class Error(val message: String) : PurchaseResult
}

interface ShopRepository {
    fun observeCoinBalance(): Flow<Int>
    fun observeOwnedItemIds(): Flow<Set<String>>
    suspend fun syncWithRemote()
    suspend fun purchase(itemId: String): PurchaseResult
    suspend fun awardGameRewards(isWin: Boolean, unlockedAchievements: Int): Int
}
