package com.example.kursach_handbook.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.kursach_handbook.data.token.TokenManager
import com.example.kursach_handbook.ui.authorization.AuthActivity
import com.example.kursach_handbook.ui.login.MainActivity
import com.example.kursach_handbook.ui.functions.isOnline
import com.example.kursach_handbook.data.local.ProfileDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = ProfileDataStore(this)
        val isDark = runBlocking { store.isDarkThemeFlow.first() }
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Сначала проверяем — есть ли Интернет?
        if (!isOnline()) {
            AlertDialog.Builder(this)
                .setTitle("Нет подключения")
                .setMessage("Пожалуйста, включите интернет.")
                .setCancelable(false)
                .setPositiveButton("Повторить") { _, _ -> recreate() }
                .setNegativeButton("Выйти") { _, _ -> finish() }
                .show()
            return
        }

        // Если онлайн — проверяем токен
        val token = TokenManager.getToken(this)
        if (token != null) {
            // Если токен существует, запускаем авторизованную активность
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // Если токен отсутствует, запускаем активность для авторизации (AuthActivity)
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }
}
