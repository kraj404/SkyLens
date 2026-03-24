package com.skylens.util

object Constants {
    // Build Config Placeholders
    const val SUPABASE_URL = "https://your-project.supabase.co"
    const val SUPABASE_ANON_KEY = "your-anon-key-here"
    const val GOOGLE_WEB_CLIENT_ID = "your-client-id.apps.googleusercontent.com"
    const val CLAUDE_API_KEY = "sk-ant-your-key-here"

    // Location Settings
    const val GPS_UPDATE_INTERVAL_MS = 5000L
    const val GPS_FASTEST_INTERVAL_MS = 3000L

    // Flight Parameters
    const val DEFAULT_CRUISE_ALTITUDE_FT = 35000
    const val MIN_FLIGHT_SPEED_KMH = 300

    // Map Settings
    const val DEFAULT_MAP_ZOOM = 8.0
    const val MAX_VISIBLE_LANDMARKS = 50

    // AI Settings
    const val NARRATOR_UPDATE_INTERVAL_MS = 30000L // 30 seconds
    const val MAX_STORY_LENGTH = 200 // words

    // Database
    const val DATABASE_NAME = "skylens.db"

    // Trip Settings
    const val MAX_PREDICTION_MINUTES = 10
}
