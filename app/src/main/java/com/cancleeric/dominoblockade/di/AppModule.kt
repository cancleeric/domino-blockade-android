package com.cancleeric.dominoblockade.di

import com.cancleeric.dominoblockade.data.analytics.AnalyticsHelper
import com.cancleeric.dominoblockade.data.preferences.GameSettingsRepositoryImpl
import com.cancleeric.dominoblockade.data.preferences.TutorialRepositoryImpl
import com.cancleeric.dominoblockade.data.repository.AchievementRepositoryImpl
import com.cancleeric.dominoblockade.data.repository.GameRecordRepositoryImpl
import com.cancleeric.dominoblockade.data.repository.GameReplayRepositoryImpl
import com.cancleeric.dominoblockade.data.repository.GameRepositoryImpl
import com.cancleeric.dominoblockade.data.repository.PlayerProfileRepositoryImpl
import com.cancleeric.dominoblockade.data.repository.PlayerStatsRepositoryImpl
import com.cancleeric.dominoblockade.data.repository.ThemeRepositoryImpl
import com.cancleeric.dominoblockade.domain.analytics.AnalyticsTracker
import com.cancleeric.dominoblockade.domain.repository.AchievementRepository
import com.cancleeric.dominoblockade.domain.repository.GameRecordRepository
import com.cancleeric.dominoblockade.domain.repository.GameReplayRepository
import com.cancleeric.dominoblockade.domain.repository.GameRepository
import com.cancleeric.dominoblockade.domain.repository.GameSettingsRepository
import com.cancleeric.dominoblockade.domain.repository.PlayerProfileRepository
import com.cancleeric.dominoblockade.domain.repository.PlayerStatsRepository
import com.cancleeric.dominoblockade.domain.repository.ThemeRepository
import com.cancleeric.dominoblockade.domain.repository.TutorialRepository
import com.cancleeric.dominoblockade.data.remote.firestore.FirestoreTournamentRepository
import com.cancleeric.dominoblockade.domain.repository.TournamentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(impl: AnalyticsHelper): AnalyticsTracker

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    @Singleton
    abstract fun bindGameRecordRepository(impl: GameRecordRepositoryImpl): GameRecordRepository

    @Binds
    @Singleton
    abstract fun bindPlayerStatsRepository(impl: PlayerStatsRepositoryImpl): PlayerStatsRepository

    @Binds
    @Singleton
    abstract fun bindGameSettingsRepository(impl: GameSettingsRepositoryImpl): GameSettingsRepository

    @Binds
    @Singleton
    abstract fun bindTutorialRepository(impl: TutorialRepositoryImpl): TutorialRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(impl: AchievementRepositoryImpl): AchievementRepository

    @Binds
    @Singleton
    abstract fun bindPlayerProfileRepository(impl: PlayerProfileRepositoryImpl): PlayerProfileRepository

    @Binds
    @Singleton
    abstract fun bindGameReplayRepository(impl: GameReplayRepositoryImpl): GameReplayRepository

    @Binds
    @Singleton
    abstract fun bindTournamentRepository(impl: FirestoreTournamentRepository): TournamentRepository
}
