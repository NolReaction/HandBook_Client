package com.example.kursach_handbook.ui.authorization

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kursach_handbook.R
import com.example.kursach_handbook.databinding.FragmentRegistrationBinding
import com.example.kursach_handbook.ui.login.MainActivity

class Registration : Fragment() {

    companion object {
        private const val PREFS = "auth_prefs"
        private const val KEY_COOLDOWN_START = "cooldown_start"
        private const val COOLDOWN_MS = 15_000L
    }

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private var countDownTimer: CountDownTimer? = null

    // ViewModel для вызова регистрации
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Скрываем/показываем навигацию
        (activity as? AuthActivity)?.hideBottomNav()

        // Восстанавливаем таймер, если он ещё идёт
        restoreCooldownIfNeeded()

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val repeatPassword = binding.repeatPasswordEditText.text.toString().trim()

            // Валидация полей
            if (email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Все поля обязательны для заполнения", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!isValidEmail(email)) {
                Toast.makeText(requireContext(), "Неверный формат электронной почты", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!isValidPassword(password)) {
                Toast.makeText(requireContext(), "Пароль должен быть длиной не менее 6 символов и содержать как буквы, так и цифры", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (password != repeatPassword) {
                Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Запускаем регистрацию
            authViewModel.register(email, password)

            // Блокируем кнопку на 15 секунд
            startCooldown(COOLDOWN_MS)
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_registration_to_guestProfile)
        }

        // Подписка на результат регистрации
        authViewModel.authEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is AuthEvent.ShowError ->
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()

                is AuthEvent.RegisterSuccess -> {
                    Toast.makeText(requireContext(), "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }

    /** Восстанавливает состояние таймера после пересоздания фрагмента */
    private fun restoreCooldownIfNeeded() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val startTimestamp = prefs.getLong(KEY_COOLDOWN_START, 0L)
        val elapsed = System.currentTimeMillis() - startTimestamp

        if (elapsed in 1 until COOLDOWN_MS) {
            // Если таймер ещё не истёк, запустим с оставшимся временем
            startCooldown(COOLDOWN_MS - elapsed)
        } else {
            // Таймер завершён или не запускался
            prefs.edit().remove(KEY_COOLDOWN_START).apply()
            binding.registerButton.isEnabled = true
            binding.registerButton.text = getString(R.string.register)
        }
    }

    /** Запускает обратный отсчёт и блокирует кнопку */
    private fun startCooldown(durationMs: Long) {
        // Сохраним стартовый таймштамп, если это первый запуск
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_COOLDOWN_START)) {
            prefs.edit()
                .putLong(KEY_COOLDOWN_START, System.currentTimeMillis())
                .apply()
        }

        binding.registerButton.isEnabled = false
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(durationMs, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.registerButton.text = "${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                binding.registerButton.text = getString(R.string.register)
                binding.registerButton.isEnabled = true
                prefs.edit().remove(KEY_COOLDOWN_START).apply()
            }
        }.start()
    }

    private fun isValidEmail(email: String): Boolean =
        email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex())

    private fun isValidPassword(password: String): Boolean =
        password.length >= 6 && password.any { it.isDigit() } && password.any { it.isLetter() }
}
