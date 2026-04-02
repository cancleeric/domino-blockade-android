package com.cancleeric.dominoblockade.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cancleeric.dominoblockade.data.local.dao.GameRecordDao
import com.cancleeric.dominoblockade.data.local.dao.PlayerStatsDao
import com.cancleeric.dominoblockade.data.local.dao.ThemeDao
import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.data.local.entity.ThemeEntity

@Database(
    entities = [GameRecordEntity::class, PlayerStatsEntity::class, ThemeEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameRecordDao(): GameRecordDao
    abstract fun playerStatsDao(): PlayerStatsDao
    abstract fun themeDao(): ThemeDao
}
