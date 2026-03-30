package com.cancleeric.dominoblockade.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cancleeric.dominoblockade.MainActivity
import com.cancleeric.dominoblockade.data.local.AppDatabase

class DominoWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(120.dp, 40.dp),  // 2x1
            DpSize(240.dp, 80.dp)   // 4x2
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val stats = loadStats(context)
        provideContent {
            GlanceTheme {
                WidgetContent(stats = stats)
            }
        }
    }

    private suspend fun loadStats(context: Context): WidgetStats {
        return try {
            val db = AppDatabase.getInstance(context)
            val dao = db.gameRecordDao()
            WidgetStats(
                totalGames = dao.getTotalGames(),
                totalWins = dao.getTotalWins()
            )
        } catch (e: Exception) {
            WidgetStats()
        }
    }

    @Composable
    private fun WidgetContent(stats: WidgetStats) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF1E3A5F)))
                .clickable(actionStartActivity<MainActivity>())
                .padding(8.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Domino Blockade",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Start Game",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFFD700)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(12.dp))
                    Text(
                        text = "Win: ${stats.winRatePercent}%",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFB0C4DE)),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
