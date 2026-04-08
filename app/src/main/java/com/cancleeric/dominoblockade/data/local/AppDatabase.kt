package com.cancleeric.dominoblockade.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cancleeric.dominoblockade.data.local.dao.AchievementDao
import com.cancleeric.dominoblockade.data.local.dao.GameRecordDao
import com.cancleeric.dominoblockade.data.local.dao.GameReplayDao
import com.cancleeric.dominoblockade.data.local.dao.PlayerProfileDao
import com.cancleeric.dominoblockade.data.local.dao.PlayerStatsDao
import com.cancleeric.dominoblockade.data.local.dao.ThemeDao
import com.cancleeric.dominoblockade.data.local.entity.AchievementEntity
import com.cancleeric.dominoblockade.data.local.entity.GameMoveEntity
import com.cancleeric.dominoblockade.data.local.entity.GameRecordEntity
import com.cancleeric.dominoblockade.data.local.entity.GameReplayEntity
import com.cancleeric.dominoblockade.data.local.entity.PlayerProfileEntity
import com.cancleeric.dominoblockade.data.local.entity.PlayerStatsEntity
import com.cancleeric.dominoblockade.data.local.entity.ThemeEntity

@Database(
    entities = [
        GameRecordEntity::class,
        PlayerStatsEntity::class,
        ThemeEntity::class,
        AchievementEntity::class,
        PlayerProfileEntity::class,
        GameReplayEntity::class,
        GameMoveEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameRecordDao(): GameRecordDao
    abstract fun playerStatsDao(): PlayerStatsDao
    abstract fun themeDao(): ThemeDao
    abstract fun achievementDao(): AchievementDao
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun gameReplayDao(): GameReplayDao
}
