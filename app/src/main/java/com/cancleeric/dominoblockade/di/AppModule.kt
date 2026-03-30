package com.cancleeric.dominoblockade.di

import android.content.Context
import androidx.room.Room
import com.cancleeric.dominoblockade.data.local.AppDatabase
import com.cancleeric.dominoblockade.data.local.GameRecordDao
import com.cancleeric.dominoblockade.data.repository.GameHistoryRepositoryImpl
import com.cancleeric.dominoblockade.data.repository.GameRepositoryImpl
import com.cancleeric.dominoblockade.domain.repository.GameHistoryRepository
import com.cancleeric.dominoblockade.domain.repository.GameRepository
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

    @Binds
    @Singleton
    abstract fun bindGameHistoryRepository(impl: GameHistoryRepositoryImpl): GameHistoryRepository

    companion object {
        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "domino_blockade.db")
                .fallbackToDestructiveMigration()
                .build()

        @Provides
        @Singleton
        fun provideGameRecordDao(db: AppDatabase): GameRecordDao = db.gameRecordDao()
    }
}
