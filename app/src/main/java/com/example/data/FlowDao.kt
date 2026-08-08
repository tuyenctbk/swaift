package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlowDao {
    @Query("SELECT * FROM flows WHERE isTemplate = 0 ORDER BY isEnabled DESC, title ASC")
    fun getAllUserFlows(): Flow<List<FlowEntity>>

    @Query("SELECT * FROM flows WHERE isTemplate = 1 ORDER BY category ASC, title ASC")
    fun getAllTemplates(): Flow<List<FlowEntity>>

    @Query("SELECT * FROM flows WHERE id = :id LIMIT 1")
    fun getFlowById(id: String): Flow<FlowEntity?>

    @Query("SELECT COUNT(*) FROM flows WHERE isTemplate = 0")
    suspend fun getUserFlowCount(): Int

    @Query("SELECT * FROM flows WHERE isTemplate = 0 ORDER BY runCount DESC LIMIT 3")
    fun getTopFrequentlyUsedFlowsSync(): List<FlowEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlow(flow: FlowEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlows(flows: List<FlowEntity>)

    @Update
    suspend fun updateFlow(flow: FlowEntity)

    @Delete
    suspend fun deleteFlow(flow: FlowEntity)

    @Query("DELETE FROM flows WHERE id = :id")
    suspend fun deleteFlowById(id: String)
}
