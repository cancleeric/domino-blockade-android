package com.cancleeric.dominoblockade.data.repository

import com.cancleeric.dominoblockade.data.local.dao.ShopDao
import com.cancleeric.dominoblockade.data.local.entity.ShopPurchaseEntity
import com.cancleeric.dominoblockade.data.local.entity.ShopWalletEntity
import com.cancleeric.dominoblockade.domain.model.ShopCatalog
import com.cancleeric.dominoblockade.domain.repository.PurchaseResult
import com.cancleeric.dominoblockade.domain.repository.ShopRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

private const val COLLECTION_SHOP_WALLETS = "shop_wallets"
private const val FIELD_COIN_BALANCE = "coinBalance"
private const val FIELD_LAST_DAILY_WIN_DATE = "lastDailyWinDate"
private const val FIELD_OWNED_ITEM_IDS = "ownedItemIds"
private const val FIELD_UPDATED_AT = "updatedAt"
private const val DAILY_FIRST_WIN_COINS = 100
private const val ACHIEVEMENT_UNLOCK_COINS = 25

@Singleton
class ShopRepositoryImpl @Inject constructor(
    private val shopDao: ShopDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ShopRepository {

    override fun observeCoinBalance(): Flow<Int> =
        shopDao.observeWallet().map { wallet -> wallet?.coinBalance ?: 0 }

    override fun observeOwnedItemIds(): Flow<Set<String>> =
        shopDao.observePurchases().map { purchases ->
            purchases.map { it.itemId }.toMutableSet().apply { add(ShopCatalog.DEFAULT_SKIN_ID) }
        }

    override suspend fun syncWithRemote() {
        val uid = ensureUid() ?: return
        val remote = firestore.collection(COLLECTION_SHOP_WALLETS).document(uid).get().await()
        if (!remote.exists()) {
            syncLocalToRemote(uid)
            return
        }
        val localWallet = shopDao.getWalletOnce() ?: ShopWalletEntity()
        val localPurchases = shopDao.getPurchasesOnce().map { it.itemId }.toSet()
        val remoteBalance = remote.getLong(FIELD_COIN_BALANCE)?.toInt() ?: 0
        val remoteDate = remote.getString(FIELD_LAST_DAILY_WIN_DATE).orEmpty()
        val remoteUpdatedAt = remote.getLong(FIELD_UPDATED_AT) ?: 0L
        val remoteOwnedIds = (remote.get(FIELD_OWNED_ITEM_IDS) as? List<*>)
            ?.filterIsInstance<String>()
            .orEmpty()
            .toSet()
        val isLocalEmpty = localWallet.updatedAt == 0L && localPurchases.isEmpty()
        if (isLocalEmpty || remoteUpdatedAt > localWallet.updatedAt) {
            shopDao.upsertWallet(
                localWallet.copy(
                    coinBalance = remoteBalance,
                    lastDailyWinDate = remoteDate,
                    updatedAt = remoteUpdatedAt
                )
            )
            val remotePurchases = remoteOwnedIds
                .filter { it != ShopCatalog.DEFAULT_SKIN_ID }
                .map { itemId -> ShopPurchaseEntity(itemId = itemId, purchasedAt = remoteUpdatedAt) }
            if (remotePurchases.isNotEmpty()) {
                shopDao.insertPurchases(remotePurchases)
            }
        } else {
            syncLocalToRemote(uid)
        }
    }

    override suspend fun purchase(itemId: String): PurchaseResult {
        val item = ShopCatalog.find(itemId) ?: return PurchaseResult.NotFound
        val owned = currentOwnedIds()
        if (item.id in owned) return PurchaseResult.AlreadyOwned
        val wallet = shopDao.getWalletOnce() ?: ShopWalletEntity()
        if (wallet.coinBalance < item.price) return PurchaseResult.InsufficientCoins
        val now = System.currentTimeMillis()
        val updatedWallet = wallet.copy(
            coinBalance = wallet.coinBalance - item.price,
            updatedAt = now
        )
        shopDao.upsertWallet(updatedWallet)
        shopDao.insertPurchase(ShopPurchaseEntity(itemId = itemId, purchasedAt = now))
        return runCatching {
            val uid = ensureUid()
            if (uid != null) {
                syncLocalToRemote(uid)
            }
            PurchaseResult.Success
        }.getOrElse { error ->
            PurchaseResult.Error(error.message ?: "Purchase sync failed")
        }
    }

    override suspend fun awardGameRewards(isWin: Boolean, unlockedAchievements: Int): Int {
        if (!isWin && unlockedAchievements <= 0) return 0
        val wallet = shopDao.getWalletOnce() ?: ShopWalletEntity()
        val today = LocalDate.now(ZoneOffset.UTC).toString()
        val dailyReward = if (isWin && wallet.lastDailyWinDate != today) DAILY_FIRST_WIN_COINS else 0
        val achievementReward = unlockedAchievements * ACHIEVEMENT_UNLOCK_COINS
        val totalReward = dailyReward + achievementReward
        if (totalReward <= 0) return 0
        val now = System.currentTimeMillis()
        val updatedWallet = wallet.copy(
            coinBalance = wallet.coinBalance + totalReward,
            lastDailyWinDate = if (dailyReward > 0) today else wallet.lastDailyWinDate,
            updatedAt = now
        )
        shopDao.upsertWallet(updatedWallet)
        ensureUid()?.let { uid ->
            runCatching { syncLocalToRemote(uid) }
        }
        return totalReward
    }

    private suspend fun currentOwnedIds(): Set<String> {
        val purchased = shopDao.getPurchasesOnce().map { it.itemId }.toMutableSet()
        purchased.add(ShopCatalog.DEFAULT_SKIN_ID)
        return purchased
    }

    private suspend fun syncLocalToRemote(uid: String) {
        val wallet = shopDao.getWalletOnce() ?: ShopWalletEntity()
        val ownedIds = currentOwnedIds()
        firestore.collection(COLLECTION_SHOP_WALLETS).document(uid).set(
            mapOf(
                FIELD_COIN_BALANCE to wallet.coinBalance,
                FIELD_LAST_DAILY_WIN_DATE to wallet.lastDailyWinDate,
                FIELD_OWNED_ITEM_IDS to ownedIds.toList(),
                FIELD_UPDATED_AT to wallet.updatedAt
            )
        ).await()
    }

    private suspend fun ensureUid(): String? {
        val current = auth.currentUser
        if (current != null) return current.uid
        return runCatching {
            auth.signInAnonymously().await().user?.uid
        }.getOrNull()
    }
}
