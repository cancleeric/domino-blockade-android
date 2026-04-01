package com.cancleeric.dominoblockade.di

import com.cancleeric.dominoblockade.data.remote.realtime.FirebaseRealtimeGameRepository
import com.cancleeric.dominoblockade.domain.repository.OnlineGameRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OnlineGameModule {

    @Binds
    @Singleton
    abstract fun bindOnlineGameRepository(
        impl: FirebaseRealtimeGameRepository
    ): OnlineGameRepository
}
