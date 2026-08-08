package com.example.data

import kotlinx.coroutines.flow.Flow

class FlowRepository(
    private val flowDao: FlowDao,
    private val historyLogDao: HistoryLogDao
) {
    val userFlows: Flow<List<FlowEntity>> = flowDao.getAllUserFlows()
    val templates: Flow<List<FlowEntity>> = flowDao.getAllTemplates()
    val historyLogs: Flow<List<HistoryLogEntity>> = historyLogDao.getAllLogs()

    suspend fun insertFlow(flow: FlowEntity) {
        flowDao.insertFlow(flow)
    }

    suspend fun updateFlow(flow: FlowEntity) {
        flowDao.updateFlow(flow)
    }

    suspend fun deleteFlow(flow: FlowEntity) {
        flowDao.deleteFlow(flow)
    }

    suspend fun deleteFlowById(id: String) {
        flowDao.deleteFlowById(id)
    }

    suspend fun addLog(log: HistoryLogEntity) {
        historyLogDao.insertLog(log)
    }

    suspend fun clearHistoryLogs() {
        historyLogDao.clearAllLogs()
    }

    suspend fun deleteLogById(id: Long) {
        historyLogDao.deleteLogById(id)
    }

    suspend fun pruneLogsOlderThan(timestamp: Long) {
        historyLogDao.deleteLogsOlderThan(timestamp)
    }
}
