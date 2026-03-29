package com.cancleeric.dominoblockade.di

import com.cancleeric.dominoblockade.domain.GameEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGameEngine(): GameEngine = GameEngine()
}
