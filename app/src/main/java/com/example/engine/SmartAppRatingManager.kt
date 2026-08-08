package com.example.engine

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.components.SmartPromptType

class SmartAppRatingManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smart_engagement_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_EXECUTION_COUNT = "key_execution_count"
        private const val KEY_HAS_RATED = "key_has_rated"
        private const val KEY_HAS_SHARED = "key_has_shared"
        private const val KEY_LAST_PROMPT_TIMESTAMP = "key_last_prompt_timestamp"
        private const val PROMPT_COOLDOWN_MS = 24 * 60 * 60 * 1000L // 24 hours cooldown between prompts
    }

    fun recordExecution() {
        val count = prefs.getInt(KEY_EXECUTION_COUNT, 0) + 1
        prefs.edit().putInt(KEY_EXECUTION_COUNT, count).apply()
    }

    fun shouldShowEngagementPrompt(activeFlowsCount: Int): SmartPromptType? {
        val hasRated = prefs.getBoolean(KEY_HAS_RATED, false)
        val hasShared = prefs.getBoolean(KEY_HAS_SHARED, false)
        val executionCount = prefs.getInt(KEY_EXECUTION_COUNT, 0)
        val lastPromptTime = prefs.getLong(KEY_LAST_PROMPT_TIMESTAMP, 0L)
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastPromptTime < PROMPT_COOLDOWN_MS) {
            return null
        }

        // Smart Calculation 1: Rate 5 Stars Prompt
        // Ideal timing: User has executed flows >= 5 times OR created >= 3 active flows
        if (!hasRated && (executionCount >= 5 || activeFlowsCount >= 3)) {
            prefs.edit().putLong(KEY_LAST_PROMPT_TIMESTAMP, currentTime).apply()
            return SmartPromptType.RATE_APP
        }

        // Smart Calculation 2: Share App Prompt
        // Ideal timing: User has executed flows >= 3 times OR has >= 2 active flows
        if (!hasShared && (executionCount >= 3 || activeFlowsCount >= 2)) {
            prefs.edit().putLong(KEY_LAST_PROMPT_TIMESTAMP, currentTime).apply()
            return SmartPromptType.SHARE_APP
        }

        return null
    }

    fun markRated() {
        prefs.edit().putBoolean(KEY_HAS_RATED, true).apply()
    }

    fun markShared() {
        prefs.edit().putBoolean(KEY_HAS_SHARED, true).apply()
    }
}
