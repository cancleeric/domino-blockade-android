package com.cancleeric.dominoblockade.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cancleeric.dominoblockade.data.local.entity.ShopPurchaseEntity
import com.cancleeric.dominoblockade.data.local.entity.ShopWalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM shop_wallet WHERE id = 1")
    fun observeWallet(): Flow<ShopWalletEntity?>

    @Query("SELECT * FROM shop_wallet WHERE id = 1")
    suspend fun getWalletOnce(): ShopWalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWallet(wallet: ShopWalletEntity)

    @Query("SELECT * FROM shop_purchases")
    fun observePurchases(): Flow<List<ShopPurchaseEntity>>

    @Query("SELECT * FROM shop_purchases")
    suspend fun getPurchasesOnce(): List<ShopPurchaseEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPurchase(purchase: ShopPurchaseEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPurchases(purchases: List<ShopPurchaseEntity>)
}
