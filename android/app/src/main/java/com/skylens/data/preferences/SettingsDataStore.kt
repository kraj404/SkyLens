package com.skylens.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.skylens.ai.AiProviderType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val AI_PROVIDER_KEY = stringPreferencesKey("ai_provider")
        val USE_METRIC_KEY = stringPreferencesKey("use_metric")
    }

    val aiProviderFlow: Flow<AiProviderType> = context.dataStore.data.map { preferences ->
        val providerName = preferences[AI_PROVIDER_KEY] ?: "GEMINI"
        try {
            AiProviderType.valueOf(providerName)
        } catch (e: Exception) {
            AiProviderType.GEMINI // Default to free option
        }
    }

    val useMetricFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_METRIC_KEY]?.toBoolean() ?: false
    }

    suspend fun setAiProvider(provider: AiProviderType) {
        context.dataStore.edit { preferences ->
            preferences[AI_PROVIDER_KEY] = provider.name
        }
    }

    suspend fun setUseMetric(useMetric: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_METRIC_KEY] = useMetric.toString()
        }
    }

    suspend fun getAiProvider(): AiProviderType {
        var result = AiProviderType.GEMINI
        context.dataStore.data.map { preferences ->
            val providerName = preferences[AI_PROVIDER_KEY] ?: "GEMINI"
            result = try {
                AiProviderType.valueOf(providerName)
            } catch (e: Exception) {
                AiProviderType.GEMINI
            }
        }
        return result
    }
}
