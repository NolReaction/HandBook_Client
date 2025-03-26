package com.example.kursach_handbook.ui.authorization

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

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    // Инициализируем ViewModel
    private val authViewModel: AuthViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        (activity as? AuthActivity)?.hideBottomNav()
    }

    override fun onPause() {
        super.onPause()
        (activity as? AuthActivity)?.showBottomNav()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Подписка на события авторизации
        authViewModel.authEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is AuthEvent.ShowError -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                }
                is AuthEvent.RegisterSuccess -> {
                    Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }
                else -> {}
            }
        }

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val repeatPassword = binding.repeatPasswordEditText.text.toString().trim()

            // Проверка заполненности всех полей
            if (email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Проверка формата email
            if (!isValidEmail(email)) {
                Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Проверка валидности пароля
            if (!isValidPassword(password)) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters and contain both letters and digits", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Проверка совпадения паролей
            if (password != repeatPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Если всё прошло проверку, вызываем регистрацию через ViewModel
            authViewModel.register(email, password)

            // Запускаем таймер, чтобы кнопка была неактивна 60 секунд
            startCooldownTimer()
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_registration_to_guestProfile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Предотвращаем утечки памяти
    }



    // Приватная функция для проверки корректности email
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return email.matches(emailRegex)
    }

    // Приватная функция для проверки валидности пароля
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6 && password.any { it.isDigit() } && password.any { it.isLetter() }
    }

    // Функция запуска таймера на 60 секунд (60000 миллисекунд)
    private fun startCooldownTimer() {
        // Отключаем кнопку
        binding.registerButton.isEnabled = false

        // Запускаем CountDownTimer с интервалом в 1 секунду
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Обновляем текст кнопки, показывая оставшееся время в секундах
                binding.registerButton.text = "${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                // По окончании таймера возвращаем текст кнопки и включаем её
                binding.registerButton.text = "Register"
                binding.registerButton.isEnabled = true
            }
        }.start()
    }

}
