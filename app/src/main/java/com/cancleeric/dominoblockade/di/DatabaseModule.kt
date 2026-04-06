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
import com.cancleeric.dominoblockade.data.local.dao.GameRecordDao
import com.cancleeric.dominoblockade.data.local.dao.PlayerProfileDao
import com.cancleeric.dominoblockade.data.local.dao.PlayerStatsDao
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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "domino_blockade.db")
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}
