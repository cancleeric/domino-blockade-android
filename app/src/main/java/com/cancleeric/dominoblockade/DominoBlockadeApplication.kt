package com.cancleeric.dominoblockade

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import java.security.MessageDigest
import java.util.Locale

private const val USER_PROPERTY_APP_VERSION = "app_version"
private const val USER_PROPERTY_LANGUAGE = "preferred_language"
private const val HASH_ALGORITHM = "SHA-256"
private const val HASHED_ID_LENGTH = 16

@HiltAndroidApp
class DominoBlockadeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initUserTracking()
    }

    private fun initUserTracking() {
        val analytics = FirebaseAnalytics.getInstance(this)
        @Suppress("DEPRECATION")
        val versionName = runCatching {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
        }.getOrDefault("unknown")
        analytics.setUserProperty(USER_PROPERTY_APP_VERSION, versionName)
        analytics.setUserProperty(USER_PROPERTY_LANGUAGE, Locale.getDefault().language)

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val uid = auth.currentUser?.uid ?: return@addAuthStateListener
            FirebaseCrashlytics.getInstance().setUserId(hashUid(uid))
        }
    }

    private fun hashUid(uid: String): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val bytes = digest.digest(uid.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(HASHED_ID_LENGTH)
    }
}
