package com.example.kursach_handbook.data.token

import android.content.Context

object TokenManager {
    private const val PREFS_NAME = "app_preferences"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_IS_VERIFIED = "is_verified"

    fun saveAuthData(context: Context, token: String, isVerified: Boolean) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putString(KEY_TOKEN, token)
            putBoolean(KEY_IS_VERIFIED, isVerified)
            apply()
        }
    }

    fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_TOKEN, null)
    }

    fun getIsVerified(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(KEY_IS_VERIFIED, false)
    }

    fun deleteAuthData(context: Context) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().remove(KEY_TOKEN).remove(KEY_IS_VERIFIED).apply()
    }
}
