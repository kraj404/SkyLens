package com.skylens.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.skylens.data.local.entities.AiUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiUsageDao {

    @Insert
    suspend fun insertUsage(usage: AiUsageEntity)

    @Query("""
        SELECT SUM(cost_usd) FROM ai_usage
        WHERE timestamp >= :startOfDayMillis
    """)
    suspend fun getTotalCostToday(startOfDayMillis: Long): Float?

    @Query("""
        SELECT COUNT(*) FROM ai_usage
        WHERE timestamp >= :startOfDayMillis
    """)
    suspend fun getTotalRequestsToday(startOfDayMillis: Long): Int

    @Query("""
        SELECT COUNT(*) FROM ai_usage
        WHERE request_type = :type AND timestamp >= :startOfDayMillis
    """)
    suspend fun getRequestCountByType(type: String, startOfDayMillis: Long): Int

    @Query("""
        SELECT * FROM ai_usage
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    fun getRecentUsage(limit: Int = 100): Flow<List<AiUsageEntity>>

    @Query("SELECT SUM(cost_usd) FROM ai_usage")
    suspend fun getTotalCostAllTime(): Float?
}
