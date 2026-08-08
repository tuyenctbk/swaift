package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FlowEntity::class, HistoryLogEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ZenFlowDatabase : RoomDatabase() {
    abstract fun flowDao(): FlowDao
    abstract fun historyLogDao(): HistoryLogDao

    companion object {
        @Volatile
        private var INSTANCE: ZenFlowDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ZenFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZenFlowDatabase::class.java,
                    "zenflow_database"
                )
                    .addCallback(ZenFlowDatabaseCallback(scope))
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class ZenFlowDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val flowDao = database.flowDao()
                    if (flowDao.getUserFlowCount() == 0) {
                        val flows = DefaultTemplates.getPrepopulatedFlows()
                        flowDao.insertFlows(flows)
                        
                        val historyDao = database.historyLogDao()
                        val now = System.currentTimeMillis()
                        val dayMs = 86_400_000L

                        // Seed historical log entries across the last 30 days for rich dashboard analytics
                        for (i in 0..28 step 2) {
                            val timeOffset = now - (i * dayMs) - ((i * 137000L) % 3600000L)
                            val flowSample = flows[i % flows.size]
                            val isSuccess = i % 5 != 0
                            historyDao.insertLog(
                                HistoryLogEntity(
                                    flowId = flowSample.id,
                                    flowTitle = flowSample.title,
                                    iconName = flowSample.iconName,
                                    colorHex = flowSample.colorHex,
                                    timestampMillis = timeOffset,
                                    status = if (isSuccess) "SUCCESS" else "FAILED",
                                    triggerReason = "Scheduled automation triggered at ${flowSample.scheduledTime}",
                                    actionsExecuted = if (isSuccess) "Executed sound profile & volume adjust" else "Device setting restricted"
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
