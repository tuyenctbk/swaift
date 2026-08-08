package com.example.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.HistoryLogDao
import com.example.data.HistoryLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Watchdog utility that monitors main-thread responsiveness.
 * If the main thread is blocked for longer than [timeoutMs], it captures
 * the main thread stack trace and logs a diagnostic entry to the local Room database.
 */
class MainThreadWatchdog(
    private val historyLogDao: HistoryLogDao,
    private val timeoutMs: Long = 2500L,
    private val checkIntervalMs: Long = 1000L
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var monitoringJob: Job? = null
    private val completedHeartbeat = AtomicBoolean(false)

    fun start() {
        if (monitoringJob?.isActive == true) return
        monitoringJob = scope.launch {
            while (isActive) {
                completedHeartbeat.set(false)
                val startTime = System.currentTimeMillis()

                mainHandler.post {
                    completedHeartbeat.set(true)
                }

                delay(checkIntervalMs)

                if (!completedHeartbeat.get()) {
                    val waitStart = System.currentTimeMillis()
                    // Wait up to timeoutMs total
                    while (!completedHeartbeat.get() && (System.currentTimeMillis() - startTime) < timeoutMs) {
                        delay(200)
                    }

                    if (!completedHeartbeat.get()) {
                        val duration = System.currentTimeMillis() - startTime
                        val mainThread = Looper.getMainLooper().thread
                        val stackTrace = mainThread.stackTrace.take(12).joinToString("\n  at ") { it.toString() }

                        Log.w("MainThreadWatchdog", "Main thread blockage detected ($duration ms):\n  at $stackTrace")

                        try {
                            historyLogDao.insertLog(
                                HistoryLogEntity(
                                    flowId = "SYSTEM_WATCHDOG",
                                    flowTitle = "Main Thread Freeze Watchdog",
                                    iconName = "Alarm",
                                    colorHex = "#EF4444",
                                    status = "ANR_WATCHDOG",
                                    triggerReason = "Main thread blocked for ${duration}ms",
                                    actionsExecuted = "Stack trace:\n  at $stackTrace"
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("MainThreadWatchdog", "Failed to log watchdog event to database", e)
                        }
                    }
                }
            }
        }
    }

    fun stop() {
        monitoringJob?.cancel()
        monitoringJob = null
        scope.coroutineContext[Job]?.cancel()
    }
}
