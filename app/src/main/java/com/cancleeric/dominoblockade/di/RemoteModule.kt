package com.cancleeric.dominoblockade.di

import com.cancleeric.dominoblockade.data.remote.auth.AuthService
import com.cancleeric.dominoblockade.data.remote.auth.FirebaseAuthService
import com.cancleeric.dominoblockade.data.remote.firestore.FirestoreLeaderboardRepository
import com.cancleeric.dominoblockade.data.remote.firestore.LeaderboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteModule {

    @Binds
    @Singleton
    abstract fun bindAuthService(impl: FirebaseAuthService): AuthService

    @Binds
    @Singleton
    abstract fun bindLeaderboardRepository(impl: FirestoreLeaderboardRepository): LeaderboardRepository
}
