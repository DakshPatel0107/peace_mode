package com.example.peacemode

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

@Suppress("NAME_SHADOWING")
class PeaceMode : Application() {

    override fun onCreate() {
        super.onCreate()
        applyThemeSettings()
    }

    private fun applyThemeSettings() {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        // Apply theme
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
