package com.cancleeric.dominoblockade.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cancleeric.dominoblockade.data.local.AppDatabase
import com.cancleeric.dominoblockade.data.local.dao.AchievementDao
import com.cancleeric.dominoblockade.data.local.dao.AdaptiveAiDao
import com.cancleeric.dominoblockade.data.local.dao.GameRecordDao
import com.cancleeric.dominoblockade.data.local.dao.GameReplayDao
import com.cancleeric.dominoblockade.data.local.dao.PlayerProfileDao
import com.cancleeric.dominoblockade.data.local.dao.PlayerStatsDao
import com.cancleeric.dominoblockade.data.local.dao.ShopDao
import com.cancleeric.dominoblockade.data.local.dao.ThemeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_settings")

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `theme_settings` " +
                "(`id` INTEGER NOT NULL, `appTheme` TEXT NOT NULL, `dominoStyle` TEXT NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `achievements` " +
                "(`type` TEXT NOT NULL, `isUnlocked` INTEGER NOT NULL DEFAULT 0, " +
                "`unlockedAt` INTEGER, PRIMARY KEY(`type`))"
        )
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `player_profiles` " +
                "(`id` INTEGER NOT NULL DEFAULT 1, `playerName` TEXT NOT NULL DEFAULT 'Player', " +
                "`avatarEmoji` TEXT NOT NULL DEFAULT '🎮', PRIMARY KEY(`id`))"
        )
    }
}

private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `game_replays` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`playerCount` INTEGER NOT NULL, " +
                "`winnerName` TEXT NOT NULL, " +
                "`isBlocked` INTEGER NOT NULL, " +
                "`timestamp` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `game_moves` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`replayId` INTEGER NOT NULL, " +
                "`moveIndex` INTEGER NOT NULL, " +
                "`playerIndex` INTEGER NOT NULL, " +
                "`playerName` TEXT NOT NULL, " +
                "`moveType` TEXT NOT NULL, " +
                "`dominoLeft` INTEGER NOT NULL, " +
                "`dominoRight` INTEGER NOT NULL, " +
                "`boardState` TEXT NOT NULL, " +
                "`boneyardSize` INTEGER NOT NULL)"
        )
    }
}

private val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `adaptive_ai_games` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`gameMode` TEXT NOT NULL, " +
                "`playerWon` INTEGER NOT NULL, " +
                "`timestamp` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `adaptive_ai_state` " +
                "(`id` INTEGER NOT NULL, `currentLevel` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "INSERT OR IGNORE INTO `adaptive_ai_state` (`id`, `currentLevel`) VALUES (1, 50)"
        )
    }
}

private val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `theme_settings` " +
                "ADD COLUMN `dominoSkin` TEXT NOT NULL DEFAULT 'CLASSIC'"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `shop_wallet` " +
                "(`id` INTEGER NOT NULL, `coinBalance` INTEGER NOT NULL, `lastDailyWinDate` TEXT NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `shop_purchases` " +
                "(`itemId` TEXT NOT NULL, `purchasedAt` INTEGER NOT NULL, PRIMARY KEY(`itemId`))"
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "domino_blockade.db")
            .addMigrations(
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8
            )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideGameRecordDao(db: AppDatabase): GameRecordDao = db.gameRecordDao()

    @Provides
    fun providePlayerStatsDao(db: AppDatabase): PlayerStatsDao = db.playerStatsDao()

    @Provides
    fun provideThemeDao(db: AppDatabase): ThemeDao = db.themeDao()

    @Provides
    fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()

    @Provides
    fun providePlayerProfileDao(db: AppDatabase): PlayerProfileDao = db.playerProfileDao()

    @Provides
    fun provideGameReplayDao(db: AppDatabase): GameReplayDao = db.gameReplayDao()

    @Provides
    fun provideAdaptiveAiDao(db: AppDatabase): AdaptiveAiDao = db.adaptiveAiDao()

    @Provides
    fun provideShopDao(db: AppDatabase): ShopDao = db.shopDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}
