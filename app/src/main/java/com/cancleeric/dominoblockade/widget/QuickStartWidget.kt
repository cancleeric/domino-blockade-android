package com.cancleeric.dominoblockade.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cancleeric.dominoblockade.MainActivity
import com.cancleeric.dominoblockade.R
import com.cancleeric.dominoblockade.ui.theme.WidgetBackground
import com.cancleeric.dominoblockade.ui.theme.WidgetButtonGreen
import com.cancleeric.dominoblockade.ui.theme.WidgetTextSecondary

class QuickStartWidget : GlanceAppWidget() {

    companion object {
        const val EXTRA_QUICK_START = "extra_quick_start"
        const val EXTRA_PLAYER_COUNT = "extra_player_count"
        const val QUICK_START_PLAYER_COUNT = 2
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(context)
        }
    }
}

@Composable
private fun WidgetContent(context: Context) {
    val launchIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(QuickStartWidget.EXTRA_QUICK_START, true)
        putExtra(QuickStartWidget.EXTRA_PLAYER_COUNT, QuickStartWidget.QUICK_START_PLAYER_COUNT)
    }
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = context.getString(R.string.widget_title),
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = context.getString(R.string.widget_status_ready),
            style = TextStyle(
                color = ColorProvider(WidgetTextSecondary),
                fontSize = 12.sp
            )
        )
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = context.getString(R.string.widget_quick_start),
            style = TextStyle(
                color = ColorProvider(WidgetBackground),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(WidgetButtonGreen)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(actionStartActivity(launchIntent))
        )
    }
}
