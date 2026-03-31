package com.cancleeric.dominoblockade.di

import com.cancleeric.dominoblockade.data.preferences.GameSettingsRepositoryImpl
import com.cancleeric.dominoblockade.data.repository.GameRecordRepositoryImpl
import com.cancleeric.dominoblockade.data.repository.GameRepositoryImpl
import com.cancleeric.dominoblockade.data.repository.PlayerStatsRepositoryImpl
import com.cancleeric.dominoblockade.domain.repository.GameRecordRepository
import com.cancleeric.dominoblockade.domain.repository.GameRepository
import com.cancleeric.dominoblockade.domain.repository.GameSettingsRepository
import com.cancleeric.dominoblockade.domain.repository.PlayerStatsRepository
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
}
