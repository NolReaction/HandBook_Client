package com.example.kursach_handbook.ui.authorization

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.kursach_handbook.R
import com.example.kursach_handbook.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    fun hideBottomNav() {
        binding.barNavAuth.visibility = View.GONE
    }

    fun showBottomNav() {
        binding.barNavAuth.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Включаем «edge-to-edge» режим (контент может уходить под статус-бар и навбар)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Инициализируем binding
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Устанавливаем слушатель для WindowInsets уже после того, как у нас есть binding.root
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Добавляем отступ сверху, равный высоте статус-бара (insets.top)
            view.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )


            // Возвращаем объект insets, чтобы не «съесть» их для других вью
            windowInsets
        }

        // 4. Настраиваем Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_auth_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.barNavAuth.setupWithNavController(navController)
    }


}