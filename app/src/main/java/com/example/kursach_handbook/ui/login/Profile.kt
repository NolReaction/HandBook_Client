package com.example.kursach_handbook.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AppCompatDelegate
import com.example.kursach_handbook.R
import com.example.kursach_handbook.data.local.ProfileDataStore
import com.example.kursach_handbook.data.model.UpdateUsernameRequest
import com.example.kursach_handbook.data.remote.AuthApi
import com.example.kursach_handbook.data.remote.RetrofitProvider
import com.example.kursach_handbook.data.token.TokenManager
import com.example.kursach_handbook.databinding.FragmentProfileBinding
import com.example.kursach_handbook.ui.authorization.AuthActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.IOException

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var store: ProfileDataStore
    private lateinit var api: AuthApi
    private lateinit var bearerToken: String
    private var currentUsername: String = ""
    private var currentAvatarKey: String = ""
    private var isDarkTheme: Boolean = false

    private val avatars = mapOf(
        "bee"      to R.drawable.bee,
        "beer"     to R.drawable.beer,
        "deer"     to R.drawable.deer,
        "fox"      to R.drawable.fox,
        "monkey"   to R.drawable.monkey,
        "owl"      to R.drawable.owl,
        "panda"    to R.drawable.panda,
        "penguin"  to R.drawable.penguin,
        "roe_deer" to R.drawable.roe_deer,
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем DataStore и API/TokenManager
        store = ProfileDataStore(requireContext())
        api = RetrofitProvider.createRetrofit(requireContext())
            .create(AuthApi::class.java)

        TokenManager.getToken(requireContext())?.let {
            bearerToken = "Bearer $it"
        } ?: run {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_LONG).show()
            requireActivity().finish()
            return
        }

        // Подписываемся на кешированные значения
        store.usernameFlow
            .onEach { name ->
                if (name.isNotBlank()) {
                    binding.usernameTextView.text = name
                }
            }
            .launchIn(lifecycleScope)

        store.avatarFlow
            .onEach { key ->
                avatars[key]?.let { resId ->
                    binding.avatarImageView.setImageResource(resId)
                }
            }
            .launchIn(lifecycleScope)

        lifecycleScope.launch {
            isDarkTheme = store.isDarkThemeFlow.first()
        }

        // Клики и другие UI-инициализации
        binding.usernameTextView.setOnClickListener { showChangeUsernameDialog() }
        binding.logoutBtn.setOnClickListener {
            TokenManager.deleteAuthData(requireContext())
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }

        binding.themeButton.setOnClickListener {
            val newTheme = !isDarkTheme
            lifecycleScope.launch {
                store.saveTheme(newTheme)
                isDarkTheme = newTheme
                AppCompatDelegate.setDefaultNightMode(
                    if (newTheme) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        // Теперь можно дергать сеть
        fetchProfile()
    }

    private fun fetchProfile() {
        lifecycleScope.launch {
            try {
                val response = api.getProfile(bearerToken)
                if (response.isSuccessful) {
                    response.body()?.let { user ->

                        // вычисляем display-name и avatarKey
                        val displayName = user.username
                            .takeIf { it.isNotBlank() }
                            ?: user.email.substringBefore("@")
                        val avatarKey = user.avatar  // строка, которую бэкенд отдаёт

                        // показываем их в UI
                        binding.usernameTextView.text = displayName
                        avatars[avatarKey]?.let {
                            binding.avatarImageView.setImageResource(it)
                        }

                        // сохраняем в локальный кеш
                        store.saveProfile(displayName, avatarKey)

                        if (!user.is_verified) {
                            Toast.makeText(
                                requireContext(),
                                "Пожалуйста подтвердите почту",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(),
                        "Ошибка сервера: ${response.code()}",
                        Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                // если нет сети — просто уведомляем, но UI уже отрисован из кеша
                Toast.makeText(requireContext(),
                    "Нет соединения !",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showChangeUsernameDialog() {
        val edit = EditText(requireContext()).apply {
            setText(currentUsername)
            setSelection(text.length)
            hint = "Новый username"
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Изменить username")
            .setMessage(
                "• От 4 до 12 символов\n" +
                        "• Только латинские буквы и цифры\n" +
                        "• Без пробелов и спецсимволов"
            )
            .setView(edit)
            .setPositiveButton("Сохранить") { dlg, _ ->
                val newName = edit.text.toString().trim()
                if (newName.isNotEmpty() && newName != currentUsername) {
                    updateUsername(newName)
                }
                dlg.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }


    private fun updateUsername(newName: String) {
        lifecycleScope.launch {
            try {
                val resp = api.updateUsername(
                    bearerToken,
                    UpdateUsernameRequest(newName)
                )
                if (resp.isSuccessful) {
                    resp.body()?.let { dto ->
                        currentUsername = dto.username
                        binding.usernameTextView.text = currentUsername
                        // сразу обновляем локальный кеш:
                        store.saveProfile(currentUsername, currentAvatarKey)
                        Toast.makeText(requireContext(),
                            "Username успешно обновлён", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // разбираем код ошибки
                    val msg = when (resp.code()) {
                        400 -> "Некорректный username"
                        409 -> "Такой username уже занят"
                        else -> "Сервер вернул ошибку: ${resp.code()}"
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                // сеть недоступна
                Toast.makeText(requireContext(),
                    "Проверьте подключение к интернету", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
