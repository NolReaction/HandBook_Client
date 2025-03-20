package com.example.kursach_handbook.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.kursach_handbook.R
import com.example.kursach_handbook.data.remote.AuthApi
import com.example.kursach_handbook.data.remote.RetrofitProvider
import com.example.kursach_handbook.data.token.TokenManager
import com.example.kursach_handbook.databinding.FragmentProfileBinding
import com.example.kursach_handbook.ui.authorization.AuthActivity
import kotlinx.coroutines.launch

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private fun fetchProfile() {
        // Получаем токен из SharedPreferences
        val token = TokenManager.getToken(requireContext())
        if (token == null) {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_LONG).show()
            return
        }

        // Создаем экземпляр API
        val authApi = RetrofitProvider.createRetrofit(requireContext()).create(AuthApi::class.java)

        // Выполняем запрос в корутине, используя lifecycleScope
        lifecycleScope.launch {
            try {
                // Отправляем запрос с заголовком Authorization
                val response = authApi.getProfile("Bearer $token")
                if (response.isSuccessful) {
                    val userDto = response.body()
                    if (userDto != null) {
                        // Если почта не подтверждена, показываем уведомление
                        if (!userDto.is_verified) {
                            Toast.makeText(requireContext(), "Please confirm your email", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error: Empty response", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchProfile()

        binding.logoutBtn.setOnClickListener {
            // Удаляем токен
            TokenManager.deleteAuthData(requireContext())
            val intent = Intent(requireContext(), AuthActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Предотвращаем утечки памяти
    }
}
