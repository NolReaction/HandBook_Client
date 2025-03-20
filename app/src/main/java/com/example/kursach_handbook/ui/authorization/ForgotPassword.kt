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
import com.example.kursach_handbook.databinding.FragmentForgotPasswordBinding

class ForgotPassword : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Подписываемся на события из ViewModel
        authViewModel.authEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is AuthEvent.ShowError -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                }
                is AuthEvent.ForgotPasswordSuccess -> {
                    Toast.makeText(requireContext(), "Reset link sent. Check your email", Toast.LENGTH_LONG).show()
                    // Возвращаем пользователя к предыдущему экрану
                    findNavController().popBackStack()
                }
                else -> { }
            }
        }

        binding.sendLetterButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Email is required", Toast.LENGTH_LONG).show()
            } else {
                authViewModel.recover(email)
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_forgotPassword_to_guestProfile_item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
