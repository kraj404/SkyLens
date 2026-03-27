package com.skylens.ai

import com.skylens.data.local.dao.AiUsageDao
import com.skylens.data.local.entities.AiUsageEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRateLimiter @Inject constructor(
    private val aiUsageDao: AiUsageDao
) {
    private val mutex = Mutex()
    private var tokens = MAX_TOKENS_PER_MINUTE
    private var lastRefillTime = System.currentTimeMillis()

    companion object {
        private const val MAX_TOKENS_PER_MINUTE = 10
        private const val DAILY_COST_LIMIT_USD = 0.10f
        private const val REFILL_INTERVAL_MS = 60_000L // 1 minute

        // Haiku pricing (per 1M tokens)
        private const val INPUT_COST_PER_MILLION = 0.25f
        private const val OUTPUT_COST_PER_MILLION = 1.25f
    }

    /**
     * Check if request is allowed based on rate limits and daily cost cap
     * Blocks if rate limit exceeded, returns false if cost limit exceeded
     */
    suspend fun checkAndConsume(requestType: String): Boolean = mutex.withLock {
        // Refill tokens if needed (token bucket algorithm)
        val now = System.currentTimeMillis()
        val timeSinceRefill = now - lastRefillTime
        if (timeSinceRefill >= REFILL_INTERVAL_MS) {
            val tokensToAdd = (timeSinceRefill / REFILL_INTERVAL_MS).toInt()
            tokens = (tokens + tokensToAdd).coerceAtMost(MAX_TOKENS_PER_MINUTE)
            lastRefillTime = now
        }

        // Check daily cost limit
        val startOfDay = getStartOfDayMillis()
        val todayCost = aiUsageDao.getTotalCostToday(startOfDay) ?: 0f
        if (todayCost >= DAILY_COST_LIMIT_USD) {
            return false
        }

        // Check token bucket
        if (tokens <= 0) {
            return false
        }

        tokens--
        return true
    }

    /**
     * Record API usage for cost tracking
     */
    suspend fun recordUsage(
        requestType: String,
        inputTokens: Int,
        outputTokens: Int,
        success: Boolean,
        errorMessage: String? = null
    ) {
        val totalTokens = inputTokens + outputTokens
        val cost = calculateCost(inputTokens, outputTokens)

        val usage = AiUsageEntity(
            requestType = requestType,
            tokensUsed = totalTokens,
            costUsd = cost,
            timestamp = System.currentTimeMillis(),
            success = success,
            errorMessage = errorMessage
        )

        aiUsageDao.insertUsage(usage)
    }

    /**
     * Get remaining requests allowed today
     */
    suspend fun getRemainingRequestsToday(): Int {
        val startOfDay = getStartOfDayMillis()
        val used = aiUsageDao.getTotalRequestsToday(startOfDay)
        val dailyLimit = MAX_TOKENS_PER_MINUTE * 24 * 60 // Theoretical max
        return (dailyLimit - used).coerceAtLeast(0)
    }

    /**
     * Get remaining budget today
     */
    suspend fun getRemainingBudgetToday(): Float {
        val startOfDay = getStartOfDayMillis()
        val used = aiUsageDao.getTotalCostToday(startOfDay) ?: 0f
        return (DAILY_COST_LIMIT_USD - used).coerceAtLeast(0f)
    }

    private fun calculateCost(inputTokens: Int, outputTokens: Int): Float {
        val inputCost = (inputTokens / 1_000_000f) * INPUT_COST_PER_MILLION
        val outputCost = (outputTokens / 1_000_000f) * OUTPUT_COST_PER_MILLION
        return inputCost + outputCost
    }

    private fun getStartOfDayMillis(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
