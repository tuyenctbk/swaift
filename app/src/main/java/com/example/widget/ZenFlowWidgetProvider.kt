package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.R
import com.example.data.FlowEntity
import com.example.data.FlowRepository
import com.example.data.ZenFlowDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ZenFlowWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_FLOW = "com.example.widget.ACTION_TOGGLE_FLOW"
        const val EXTRA_FLOW_ID = "com.example.widget.EXTRA_FLOW_ID"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.zenflow_widget_layout)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = ZenFlowDatabase.getDatabase(context.applicationContext, this)
                    val repository = FlowRepository(database.flowDao(), database.historyLogDao())
                    val topFlows = database.flowDao().getTopFrequentlyUsedFlowsSync()

                    if (topFlows.isEmpty()) {
                        views.setViewVisibility(R.id.widget_empty_text, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_row_1, View.GONE)
                        views.setViewVisibility(R.id.widget_divider_1, View.GONE)
                        views.setViewVisibility(R.id.widget_row_2, View.GONE)
                        views.setViewVisibility(R.id.widget_divider_2, View.GONE)
                        views.setViewVisibility(R.id.widget_row_3, View.GONE)
                    } else {
                        views.setViewVisibility(R.id.widget_empty_text, View.GONE)

                        // Bind Flow 1
                        views.setViewVisibility(R.id.widget_row_1, View.VISIBLE)
                        views.setTextViewText(R.id.widget_flow_title_1, topFlows[0].title)
                        views.setTextViewText(R.id.widget_flow_cat_1, "${topFlows[0].category} • ${if (topFlows[0].isEnabled) "ACTIVE" else "PAUSED"}")
                        views.setTextViewText(R.id.widget_icon_1, getEmojiForIcon(topFlows[0].iconName))
                        views.setTextViewText(R.id.widget_action_btn_1, if (topFlows[0].isEnabled) "Pause" else "Start")
                        
                        val intent1 = Intent(context, ZenFlowWidgetProvider::class.java).apply {
                            action = ACTION_TOGGLE_FLOW
                            putExtra(EXTRA_FLOW_ID, topFlows[0].id)
                        }
                        val pendingIntent1 = PendingIntent.getBroadcast(
                            context,
                            topFlows[0].id.hashCode(),
                            intent1,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_action_btn_1, pendingIntent1)

                        // Bind Flow 2
                        if (topFlows.size > 1) {
                            views.setViewVisibility(R.id.widget_divider_1, View.VISIBLE)
                            views.setViewVisibility(R.id.widget_row_2, View.VISIBLE)
                            views.setTextViewText(R.id.widget_flow_title_2, topFlows[1].title)
                            views.setTextViewText(R.id.widget_flow_cat_2, "${topFlows[1].category} • ${if (topFlows[1].isEnabled) "ACTIVE" else "PAUSED"}")
                            views.setTextViewText(R.id.widget_icon_2, getEmojiForIcon(topFlows[1].iconName))
                            views.setTextViewText(R.id.widget_action_btn_2, if (topFlows[1].isEnabled) "Pause" else "Start")

                            val intent2 = Intent(context, ZenFlowWidgetProvider::class.java).apply {
                                action = ACTION_TOGGLE_FLOW
                                putExtra(EXTRA_FLOW_ID, topFlows[1].id)
                            }
                            val pendingIntent2 = PendingIntent.getBroadcast(
                                context,
                                topFlows[1].id.hashCode(),
                                intent2,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            views.setOnClickPendingIntent(R.id.widget_action_btn_2, pendingIntent2)
                        } else {
                            views.setViewVisibility(R.id.widget_divider_1, View.GONE)
                            views.setViewVisibility(R.id.widget_row_2, View.GONE)
                        }

                        // Bind Flow 3
                        if (topFlows.size > 2) {
                            views.setViewVisibility(R.id.widget_divider_2, View.VISIBLE)
                            views.setViewVisibility(R.id.widget_row_3, View.VISIBLE)
                            views.setTextViewText(R.id.widget_flow_title_3, topFlows[2].title)
                            views.setTextViewText(R.id.widget_flow_cat_3, "${topFlows[2].category} • ${if (topFlows[2].isEnabled) "ACTIVE" else "PAUSED"}")
                            views.setTextViewText(R.id.widget_icon_3, getEmojiForIcon(topFlows[2].iconName))
                            views.setTextViewText(R.id.widget_action_btn_3, if (topFlows[2].isEnabled) "Pause" else "Start")

                            val intent3 = Intent(context, ZenFlowWidgetProvider::class.java).apply {
                                action = ACTION_TOGGLE_FLOW
                                putExtra(EXTRA_FLOW_ID, topFlows[2].id)
                            }
                            val pendingIntent3 = PendingIntent.getBroadcast(
                                context,
                                topFlows[2].id.hashCode(),
                                intent3,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            views.setOnClickPendingIntent(R.id.widget_action_btn_3, pendingIntent3)
                        } else {
                            views.setViewVisibility(R.id.widget_divider_2, View.GONE)
                            views.setViewVisibility(R.id.widget_row_3, View.GONE)
                        }
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    android.util.Log.e("ZenFlowWidgetProvider", "Error updating widget", e)
                }
            }
        }

        private fun getEmojiForIcon(iconName: String): String {
            return when (iconName) {
                "Bedtime" -> "🌙"
                "Work" -> "💼"
                "FitnessCenter" -> "🏋️"
                "DirectionsCar" -> "🚗"
                "BatterySaver" -> "🔋"
                "PlayCircle" -> "🎵"
                "Home" -> "🏠"
                "Wifi" -> "📶"
                "Bluetooth" -> "📡"
                "LocationOn" -> "📍"
                "Schedule" -> "⏰"
                "Alarm" -> "🔔"
                "VolumeUp" -> "🔊"
                "Brightness6" -> "🔅"
                "NotificationsActive" -> "💡"
                else -> "⚡"
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_FLOW) {
            val flowId = intent.getStringExtra(EXTRA_FLOW_ID) ?: return
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = ZenFlowDatabase.getDatabase(context.applicationContext, this)
                    val repository = FlowRepository(database.flowDao(), database.historyLogDao())
                    val topFlows = database.flowDao().getTopFrequentlyUsedFlowsSync()
                    val flow = topFlows.find { it.id == flowId }
                    if (flow != null) {
                        val updated = flow.copy(isEnabled = !flow.isEnabled)
                        repository.updateFlow(updated)
                        
                        // Alarm scheduler synchronization
                        val trigger = com.example.data.JsonUtils.deserializeTrigger(updated.triggerConfigJson)
                        if (updated.isEnabled && updated.triggerType == com.example.data.TriggerType.SCHEDULE && trigger.useAlarmManager) {
                            com.example.service.AlarmScheduler.scheduleAlarm(context.applicationContext, updated)
                        } else {
                            com.example.service.AlarmScheduler.cancelAlarm(context.applicationContext, updated)
                        }

                        // Force immediate visual update of all active widgets
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val thisAppWidget = ComponentName(context.packageName, ZenFlowWidgetProvider::class.java.name)
                        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
                        for (appWidgetId in appWidgetIds) {
                            updateAppWidget(context, appWidgetManager, appWidgetId)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ZenFlowWidgetProvider", "Error toggling flow in widget", e)
                }
            }
        }
    }
}
