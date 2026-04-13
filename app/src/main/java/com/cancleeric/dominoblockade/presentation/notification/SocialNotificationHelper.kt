package com.cancleeric.dominoblockade.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cancleeric.dominoblockade.MainActivity
import com.cancleeric.dominoblockade.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "social_challenges"
private const val CHANNEL_NAME = "Social Challenges"
private const val CHANNEL_DESCRIPTION = "Friend requests and challenge invitations"
private const val NOTIFICATION_ID_BASE = 4000

@Singleton
class SocialNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    init {
        createNotificationChannel()
    }

    fun showChallengeNotification(
        challengeId: String,
        challengerName: String,
        gameMode: String,
        acceptDeepLink: String,
        declineDeepLink: String
    ) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val acceptIntent = deepLinkIntent(acceptDeepLink, challengeId.hashCode())
        val declineIntent = deepLinkIntent(declineDeepLink, challengeId.hashCode() + 1)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Challenge from $challengerName")
            .setContentText("$challengerName invited you to a $gameMode game")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(acceptIntent)
            .addAction(0, "Accept", acceptIntent)
            .addAction(0, "Decline", declineIntent)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + challengeId.hashCode(), notification)
    }

    private fun deepLinkIntent(deepLink: String, requestCode: Int): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink), context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        channel.description = CHANNEL_DESCRIPTION
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
