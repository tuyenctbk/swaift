package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.FlowRepository
import com.example.data.HistoryLogEntity
import com.example.data.TriggerType
import com.example.data.ZenFlowDatabase
import com.example.engine.EnvironmentSimulator
import com.example.engine.FlowExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutomationForegroundService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("SwAIft Background Engine active"))
        startScheduledRoutineLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startScheduledRoutineLoop() {
        serviceScope.launch {
            val database = ZenFlowDatabase.getDatabase(applicationContext, this)
            val repository = FlowRepository(database.flowDao(), database.historyLogDao())

            while (isActive) {
                try {
                    val activeFlows = repository.userFlows.first().filter { it.isEnabled }
                    val nowMs = System.currentTimeMillis()
                    val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(nowMs))
                    val currentEnv = EnvironmentSimulator.environment.value.copy(timeOfDay = currentTimeStr)

                    var triggeredInLoop = 0
                    activeFlows.forEach { flow ->
                        val isTimeMatched = flow.triggerType == TriggerType.SCHEDULE && flow.scheduledTime == currentTimeStr
                        val isAlreadyRunThisMinute = flow.lastRunTimeMillis?.let { lastRun ->
                            val lastRunMinute = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(lastRun))
                            val currentMinute = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(nowMs))
                            lastRunMinute == currentMinute
                        } ?: false

                        if ((isTimeMatched || FlowExecutor.isTriggerMatched(flow, currentEnv)) && !isAlreadyRunThisMinute) {
                            val result = FlowExecutor.executeFlow(flow, reasonOverride = "Foreground Scheduled Time Trigger ($currentTimeStr)")
                            
                            val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                            val isPowerSave = powerManager?.isPowerSaveMode == true
                            val isFailure = isPowerSave && (flow.category == "Battery" || flow.category == "Connectivity")

                            if (isFailure) {
                                repository.addLog(
                                    HistoryLogEntity(
                                        flowId = flow.id,
                                        flowTitle = flow.title,
                                        iconName = flow.iconName,
                                        colorHex = flow.colorHex,
                                        status = "FAILED",
                                        triggerReason = "Time Trigger matched ($currentTimeStr)",
                                        actionsExecuted = "Failed: Background execution restricted by OS Power Save Mode"
                                    )
                                )
                            } else {
                                val updated = flow.copy(
                                    lastRunTimeMillis = nowMs,
                                    runCount = flow.runCount + 1
                                )
                                repository.updateFlow(updated)
                                repository.addLog(
                                    HistoryLogEntity(
                                        flowId = flow.id,
                                        flowTitle = flow.title,
                                        iconName = flow.iconName,
                                        colorHex = flow.colorHex,
                                        status = "SUCCESS",
                                        triggerReason = "Time Trigger matched ($currentTimeStr)",
                                        actionsExecuted = result.actionsSummary
                                    )
                                )
                                triggeredInLoop++
                            }
                        }
                    }

                    if (triggeredInLoop > 0) {
                        updateNotification("Triggered $triggeredInLoop routine(s) at $currentTimeStr")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AutomationForegroundService", "Error in service loop", e)
                }

                val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                val isPowerSave = powerManager?.isPowerSaveMode == true
                val pollInterval = if (isPowerSave) 60_000L else 15_000L
                delay(pollInterval)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SwAIft Automation Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors local triggers and executes scheduled routines."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("⚡ SwAIft Active")
        .setContentText(text)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    companion object {
        const val CHANNEL_ID = "zenflow_engine_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_SERVICE = "com.example.service.STOP_AUTOMATION"

        fun startService(context: Context) {
            val intent = Intent(context, AutomationForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AutomationForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
