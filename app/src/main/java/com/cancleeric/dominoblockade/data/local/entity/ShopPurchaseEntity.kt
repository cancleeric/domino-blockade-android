package com.cancleeric.dominoblockade.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_purchases")
data class ShopPurchaseEntity(
    @PrimaryKey val itemId: String,
    val purchasedAt: Long
)
