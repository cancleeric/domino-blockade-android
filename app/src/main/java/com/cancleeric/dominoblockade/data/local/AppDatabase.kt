package com.cancleeric.dominoblockade.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cancleeric.dominoblockade.data.local.dao.AchievementDao
import com.cancleeric.dominoblockade.data.local.dao.GameRecordDao
import com.cancleeric.dominoblockade.data.local.dao.PlayerStatsDao
import com.cancleeric.dominoblockade.data.local.entity.AchievementEntity
import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity

@Database(
    entities = [GameRecordEntity::class, PlayerStatsEntity::class, AchievementEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameRecordDao(): GameRecordDao
    abstract fun playerStatsDao(): PlayerStatsDao
    abstract fun achievementDao(): AchievementDao
}
