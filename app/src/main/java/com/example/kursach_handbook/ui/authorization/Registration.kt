package com.example.kursach_handbook.ui.authorization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kursach_handbook.R
import com.example.kursach_handbook.databinding.FragmentGuestProfileBinding
import com.example.kursach_handbook.databinding.FragmentRegistrationBinding

class Registration : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Подписка на события (например, для регистрации)
        authViewModel.authEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is AuthEvent.ShowError -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                }
                is AuthEvent.RegisterSuccess -> {
                    Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registration_to_guestProfile)
                }
                else -> {}
            }
        }

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val repeatPassword = binding.repeatPasswordEditText.text.toString().trim()

            // Проверка повторного ввода пароля
            if (repeatPassword != password) {
                Toast.makeText(requireContext(), "Incorrect repeat password", Toast.LENGTH_LONG).show()
                return@setOnClickListener // Прерываем выполнение, если пароли не совпадают
            }

            // Проверка заполненности email и password
            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.register(email, password)
            } else {
                Toast.makeText(requireContext(), "Incorrect Input", Toast.LENGTH_LONG).show()
            }
        }

        binding.backButton.setOnClickListener{
            findNavController().navigate(R.id.action_registration_to_guestProfile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
