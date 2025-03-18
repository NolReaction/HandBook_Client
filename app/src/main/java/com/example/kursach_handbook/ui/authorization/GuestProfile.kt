package com.example.kursach_handbook.ui.authorization

import android.content.Intent
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
import com.example.kursach_handbook.ui.login.MainActivity

class GuestProfile : Fragment() {
    // _binding хранит ссылку на биндинг, а binding используется для доступа к view
    private var _binding: FragmentGuestProfileBinding? = null
    private val binding get() = _binding!!

    // Инициализация ViewModel
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuestProfileBinding.inflate(inflater, container, false)
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
                is AuthEvent.LoginSuccess -> {
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                    // Запускаем авторизованную активность
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    // Завершаем текущую (неавторизованную) активность
                    requireActivity().finish()
                }
            }
        }

        binding.registerLinkTextView.setOnClickListener {
            findNavController().navigate(R.id.action_guestProfile_item_to_registration)
        }

        binding.forgotPasswordTextView.setOnClickListener {
            findNavController().navigate(R.id.action_guestProfile_item_to_forgotPassword)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.login(email, password)
            } else {
                Toast.makeText(requireContext(), "Incorrect Input", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Предотвращаем утечки памяти
    }
}
