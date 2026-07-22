package com.hedaro.musicplayer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedaro.musicplayer.data.preferences.SettingsRepository
import com.hedaro.musicplayer.data.preferences.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Exposes and updates app settings. Also read at the app root to apply the theme. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    // null = preference not loaded from disk yet. The splash screen stays up until this is
    // non-null, so the first visible frame is already correctly themed (no flicker).
    // Eagerly so loading starts as soon as the ViewModel is created (in the Activity).
    val themeMode: StateFlow<ThemeMode?> =
        settingsRepository.themeMode
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }
}
