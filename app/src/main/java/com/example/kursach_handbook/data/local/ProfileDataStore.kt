package com.example.kursach_handbook.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. Свойство-расширение для доступа к DataStore
private val Context.dataStore by preferencesDataStore("user_prefs")

class ProfileDataStore(private val context: Context) {
    companion object {
        private val KEY_USERNAME = stringPreferencesKey("key_username")
        private val KEY_AVATAR   = stringPreferencesKey("key_avatar")
        private val KEY_DARK_THEME = booleanPreferencesKey("key_dark_theme")
    }

    /** Flow текущего username (может быть пустая строка) */
    val usernameFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_USERNAME] ?: "" }

    /** Flow текущего avatar (строка-ключ из мапы) */
    val avatarFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_AVATAR] ?: "" }

    /** Flow текущей темы (true - тёмная) */
    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_DARK_THEME] ?: false }

    /** Сохранить   новые данные */
    suspend fun saveProfile(username: String, avatarKey: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
            prefs[KEY_AVATAR]   = avatarKey
        }
    }

    /** Сохранить выбор темы */
    suspend fun saveTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DARK_THEME] = isDark
        }
    }
}
