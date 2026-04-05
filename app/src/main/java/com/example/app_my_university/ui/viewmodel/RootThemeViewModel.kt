package com.example.app_my_university.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_my_university.data.theme.AppThemePreference
import com.example.app_my_university.data.theme.ThemePreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RootThemeViewModel @Inject constructor(
    themePreferenceRepository: ThemePreferenceRepository,
) : ViewModel() {

    val themePreference: StateFlow<AppThemePreference> =
        themePreferenceRepository.observeThemePreference()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AppThemePreference.SYSTEM,
            )
}
