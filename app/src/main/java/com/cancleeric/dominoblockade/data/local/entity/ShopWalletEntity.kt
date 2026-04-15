package com.cancleeric.dominoblockade.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_wallet")
data class ShopWalletEntity(
    @PrimaryKey val id: Int = 1,
    val coinBalance: Int = 0,
    val lastDailyWinDate: String = "",
    val updatedAt: Long = 0L
)
