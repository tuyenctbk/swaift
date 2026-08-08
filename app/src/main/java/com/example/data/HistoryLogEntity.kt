package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_logs")
data class HistoryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val flowId: String,
    val flowTitle: String,
    val iconName: String,
    val colorHex: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS", // SUCCESS, TRIGGERED, SKIPPED, MANUAL
    val triggerReason: String,
    val actionsExecuted: String
)
