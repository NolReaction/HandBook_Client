package com.example.kursach_handbook.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. Свойство-расширение для доступа к DataStore
private val Context.dataStore by preferencesDataStore("user_prefs")

class ProfileDataStore(private val context: Context) {
    companion object {
        private val KEY_USERNAME = stringPreferencesKey("key_username")
        private val KEY_AVATAR   = stringPreferencesKey("key_avatar")
    }

    /** Flow текущего username (может быть пустая строка) */
    val usernameFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_USERNAME] ?: "" }

    /** Flow текущего avatar (строка-ключ из мапы) */
    val avatarFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_AVATAR] ?: "" }

    /** Сохранить новые данные */
    suspend fun saveProfile(username: String, avatarKey: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
            prefs[KEY_AVATAR]   = avatarKey
        }
    }
}
