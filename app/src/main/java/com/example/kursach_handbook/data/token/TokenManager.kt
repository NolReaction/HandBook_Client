package com.example.kursach_handbook.data.token

import android.content.Context

class TokenManager {
    fun saveToken(context: Context, token: String) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        sharedPref.edit().putString("auth_token", token).apply()
    }

    fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }

    // Функция для удаления токена (logout)
    fun deleteToken(context: Context) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        sharedPref.edit().remove("auth_token").apply()
    }
}