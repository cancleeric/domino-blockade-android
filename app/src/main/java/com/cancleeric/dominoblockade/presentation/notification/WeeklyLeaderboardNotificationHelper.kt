package com.cancleeric.dominoblockade.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cancleeric.dominoblockade.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "weekly_leaderboard"
private const val CHANNEL_NAME = "Weekly Leaderboard"
private const val CHANNEL_DESCRIPTION = "Rank change alerts for the weekly challenge leaderboard"
private const val NOTIFICATION_ID = 6000
private const val TOP_10_RANK = 10

@Singleton
class WeeklyLeaderboardNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) : RankChangeNotifier {

    init {
        createNotificationChannel()
    }

    override fun notifyRankChange(newRank: Int, previousRank: Int?) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val enteredTop10 = newRank <= TOP_10_RANK && (previousRank == null || previousRank > TOP_10_RANK)
        val exitedTop10 = newRank > TOP_10_RANK && previousRank != null && previousRank <= TOP_10_RANK
        val (title, body) = when {
            enteredTop10 -> "You entered the Top 10! 🏆" to "You are now ranked #$newRank in the Weekly Challenge."
            exitedTop10 -> "You dropped out of the Top 10" to "You are now ranked #$newRank. Keep playing to climb back!"
            else -> return
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        channel.description = CHANNEL_DESCRIPTION
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
