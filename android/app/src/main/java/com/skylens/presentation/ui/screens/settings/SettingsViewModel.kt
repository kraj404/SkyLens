package com.skylens.presentation.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skylens.ai.AiProviderType
import com.skylens.data.preferences.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val aiProvider: StateFlow<AiProviderType> = settingsDataStore.aiProviderFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AiProviderType.GEMINI
        )

    val useMetric: StateFlow<Boolean> = settingsDataStore.useMetricFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setAiProvider(provider: AiProviderType) {
        viewModelScope.launch {
            settingsDataStore.setAiProvider(provider)
        }
    }

    fun setUseMetric(useMetric: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setUseMetric(useMetric)
        }
    }
}
