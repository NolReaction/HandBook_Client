package com.example.kursach_handbook.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.kursach_handbook.R
import com.example.kursach_handbook.data.token.TokenManager
import com.example.kursach_handbook.databinding.FragmentProfileBinding
import com.example.kursach_handbook.ui.authorization.AuthActivity

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutBtn.setOnClickListener {
            // Удаляем токен
            TokenManager().deleteToken(requireContext())
            // Перенаправляем пользователя на экран авторизации (или стартовую активность)
            val intent = Intent(requireContext(), AuthActivity::class.java)
            startActivity(intent)
            // Завершаем текущую активность
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Предотвращаем утечки памяти
    }
}
