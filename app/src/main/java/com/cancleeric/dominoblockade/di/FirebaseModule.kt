package com.cancleeric.dominoblockade.di

import android.app.Application
import com.cancleeric.dominoblockade.analytics.AnalyticsService
import com.cancleeric.dominoblockade.analytics.FirebaseAnalyticsService
import com.cancleeric.dominoblockade.data.remote.auth.AuthService
import com.cancleeric.dominoblockade.data.remote.auth.FirebaseAuthService
import com.cancleeric.dominoblockade.data.remote.firestore.FirestoreLeaderboardRepository
import com.cancleeric.dominoblockade.data.remote.firestore.LeaderboardRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAnalytics(app: Application): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(app)

    @Provides
    @Singleton
    fun provideCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseBindingsModule {

    @Binds
    @Singleton
    abstract fun bindAuthService(impl: FirebaseAuthService): AuthService

    @Binds
    @Singleton
    abstract fun bindLeaderboardRepository(
        impl: FirestoreLeaderboardRepository
    ): LeaderboardRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsService(impl: FirebaseAnalyticsService): AnalyticsService
}
