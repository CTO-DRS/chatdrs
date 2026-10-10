package com.drs.chatdrs.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "chatdrs_theme_preferences"
)

data class ThemePreferences(
    val isDarkMode: Boolean?,
    val useDynamicColor: Boolean = true
)

/**
 * Persists user theme preferences (Light/Dark mode and Material 3 Dynamic Color)
 * using Jetpack DataStore Preferences.
 */
class ThemePreferencesRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.applicationContext.themeDataStore)

    val themePreferencesFlow: Flow<ThemePreferences> = dataStore.data.map { preferences ->
        ThemePreferences(
            isDarkMode = preferences[IS_DARK_MODE_KEY],
            useDynamicColor = preferences[USE_DYNAMIC_COLOR_KEY] ?: true
        )
    }

    suspend fun setDarkMode(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDark
        }
    }

    suspend fun toggleDarkMode(currentDarkState: Boolean) {
        setDarkMode(!currentDarkState)
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLOR_KEY] = enabled
        }
    }

    companion object {
        val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        val USE_DYNAMIC_COLOR_KEY = booleanPreferencesKey("use_dynamic_color")
    }
}
