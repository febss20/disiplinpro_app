package com.example.disiplinpro.viewmodel.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disiplinpro.data.preferences.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel : ViewModel() {
    private lateinit var themePreferences: ThemePreferences

    lateinit var isDarkMode: StateFlow<Boolean>

    fun initialize(context: Context) {
        themePreferences = ThemePreferences(context)

        isDarkMode = themePreferences.isDarkMode
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = false
            )
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            themePreferences.setDarkMode(!isDarkMode.value)
        }
    }
}