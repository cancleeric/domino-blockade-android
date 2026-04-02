package com.cancleeric.dominoblockade.startup

import android.content.Context
import android.os.SystemClock
import androidx.startup.Initializer

/**
 * Records the elapsed real-time at the earliest possible point in the cold-start sequence.
 *
 * The captured value ([AppTimingInitializer.startElapsedRealtime]) can be consumed anywhere
 * in the app to calculate the time-to-interactive without attaching an extra ContentProvider.
 */
object AppStartupStats {
    /** Elapsed real-time in milliseconds at the start of [AppTimingInitializer.create]. */
    var startElapsedRealtime: Long = 0L
        internal set
}

class AppTimingInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        AppStartupStats.startElapsedRealtime = SystemClock.elapsedRealtime()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
