package com.cancleeric.dominoblockade.di

import com.cancleeric.dominoblockade.data.remote.realtime.FirebaseRealtimeGameRepository
import com.cancleeric.dominoblockade.domain.repository.OnlineGameRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

const val RECONNECT_TIMEOUT_SECONDS = 30

@Module
@InstallIn(SingletonComponent::class)
abstract class OnlineGameModule {

    @Binds
    @Singleton
    abstract fun bindOnlineGameRepository(
        impl: FirebaseRealtimeGameRepository
    ): OnlineGameRepository

    companion object {
        @Provides
        @Named("reconnectTimeout")
        fun provideReconnectTimeoutSeconds(): Int = RECONNECT_TIMEOUT_SECONDS
    }
}
