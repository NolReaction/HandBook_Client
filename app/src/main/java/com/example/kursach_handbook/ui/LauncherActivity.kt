package com.example.kursach_handbook.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kursach_handbook.R
import com.example.kursach_handbook.data.token.TokenManager
import com.example.kursach_handbook.ui.authorization.AuthActivity
import com.example.kursach_handbook.ui.login.MainActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = TokenManager().getToken(this)
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