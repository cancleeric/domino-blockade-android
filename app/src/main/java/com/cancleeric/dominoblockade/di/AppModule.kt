package com.cancleeric.dominoblockade.di

import android.content.Context
import com.cancleeric.dominoblockade.data.preferences.TutorialPreferencesRepository
import com.cancleeric.dominoblockade.data.repository.GameRepositoryImpl
import com.cancleeric.dominoblockade.domain.repository.GameRepository
import com.cancleeric.dominoblockade.domain.repository.TutorialRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    companion object {
        @Provides
        @Singleton
        fun provideTutorialPreferencesRepository(
            @ApplicationContext context: Context
        ): TutorialRepository = TutorialPreferencesRepository(context)
    }
}
