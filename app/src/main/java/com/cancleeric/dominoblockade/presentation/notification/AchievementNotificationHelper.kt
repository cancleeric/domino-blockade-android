package com.cancleeric.dominoblockade.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cancleeric.dominoblockade.domain.model.AchievementType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "achievement_channel"
private const val CHANNEL_NAME = "Achievements"
private const val NOTIFICATION_BASE_ID = 1000

@Singleton
class AchievementNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        createNotificationChannel()
    }

    fun showAchievementUnlocked(type: AchievementType) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.star_on)
            .setContentTitle("Achievement Unlocked!")
            .setContentText("${type.badge} ${type.title}: ${type.description}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_BASE_ID + type.ordinal, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "Notifications for unlocked achievements"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
