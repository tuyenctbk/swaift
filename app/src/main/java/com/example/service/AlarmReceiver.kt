package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.FlowRepository
import com.example.data.HistoryLogEntity
import com.example.data.ZenFlowDatabase
import com.example.engine.FlowExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val flowId = intent.getStringExtra("FLOW_ID") ?: return
        
        val goAsync = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = ZenFlowDatabase.getDatabase(context.applicationContext, this)
                val repository = FlowRepository(database.flowDao(), database.historyLogDao())
                val flow = repository.userFlows.first().find { it.id == flowId }
                
                if (flow != null && flow.isEnabled) {
                    val nowMs = System.currentTimeMillis()
                    val result = FlowExecutor.executeFlow(flow, reasonOverride = "AlarmManager Precise Time Trigger reached")
                    
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
                            triggerReason = "AlarmManager Precise Time Trigger reached",
                            actionsExecuted = result.actionsSummary
                        )
                    )
                    
                    // Reschedule alarm for the next occurrence tomorrow
                    AlarmScheduler.scheduleAlarm(context.applicationContext, flow)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                goAsync.finish()
            }
        }
    }
}
