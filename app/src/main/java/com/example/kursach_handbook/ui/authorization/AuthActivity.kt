package com.example.kursach_handbook.ui.authorization

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.kursach_handbook.R
import com.example.kursach_handbook.databinding.ActivityAuthBinding
import androidx.core.view.get
import androidx.core.view.size

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем NavController из правильного NavHostFragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_auth_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Привязываем BottomNavigationView к NavController
        binding.barNavAuth.setupWithNavController(navController)

        // Аккуратно раскладываем системные отступы (status/navigation bars)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, 0)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.barNavAuth) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, sys.bottom)
            insets
        }

        // Показываем бар только на тех destination, которые соответствуют пунктам меню bottom nav
        val bottomDestinations: Set<Int> = buildSet {
            val menu = binding.barNavAuth.menu
            for (i in 0 until menu.size) add(menu[i].itemId)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.barNavAuth.isVisible = destination.id in bottomDestinations
        }
    }

    // Оставим для обратной совместимости (лучше не вызывать из фрагментов)
    fun hideBottomNav() {
        binding.barNavAuth.isVisible = false
    }

    fun showBottomNav() {
        binding.barNavAuth.isVisible = true
    }
}
