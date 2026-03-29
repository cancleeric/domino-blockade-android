package com.cancleeric.dominoblockade

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DominoBlockadeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Enable Crashlytics in non-debug builds
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}
