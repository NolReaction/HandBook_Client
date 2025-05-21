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
import com.example.kursach_handbook.R
import com.example.kursach_handbook.data.model.UpdateUsernameRequest
import com.example.kursach_handbook.data.remote.AuthApi
import com.example.kursach_handbook.data.remote.RetrofitProvider
import com.example.kursach_handbook.data.token.TokenManager
import com.example.kursach_handbook.databinding.FragmentProfileBinding
import com.example.kursach_handbook.ui.authorization.AuthActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.IOException

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var api: AuthApi
    private lateinit var bearerToken: String
    private var currentUsername: String = ""

    private val avatars = mapOf(
        "bee"      to R.drawable.bee,
        "beer"      to R.drawable.beer,
        "deer"      to R.drawable.deer,
        "fox"      to R.drawable.fox,
        "monkey"      to R.drawable.monkey,
        "owl"      to R.drawable.owl,
        "panda"      to R.drawable.panda,
        "penguin"      to R.drawable.penguin,
        "roe_deer"      to R.drawable.roe_deer,
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

        api = RetrofitProvider
            .createRetrofit(requireContext())
            .create(AuthApi::class.java)

        // достаём токен
        TokenManager.getToken(requireContext())?.let {
            bearerToken = "Bearer $it"
        } ?: run {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_LONG).show()
            return
        }

        binding.usernameTextView.setOnClickListener { showChangeUsernameDialog() }
        binding.logoutBtn.setOnClickListener {
            TokenManager.deleteAuthData(requireContext())
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }

        fetchProfile()
    }

    private fun fetchProfile() {
        lifecycleScope.launch {
            try {
                val r = api.getProfile(bearerToken)
                if (r.isSuccessful) {
                    r.body()?.let { user ->
                        // Аватар из БД
                        val resId = avatars[user.avatar]
                            ?: R.drawable.default_avatar  // запасной вариант, если вдруг ключа нет
                        binding.avatarImageView.setImageResource(resId)

                        // username: либо то, что пришло, либо берём часть email
                        currentUsername = user.username
                            .takeIf { it?.isNotBlank() == true }
                            ?: user.email.substringBefore("@")
                        binding.usernameTextView.text = currentUsername

                        if (!user.is_verified) {
                            Toast.makeText(
                                requireContext(),
                                "Please confirm your email",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${r.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showChangeUsernameDialog() {
        val edit = EditText(requireContext()).apply {
            setText(currentUsername)
            setSelection(text.length)
            hint = "New username"
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change username")
            .setView(edit)
            .setPositiveButton("Save") { dlg, _ ->
                val newName = edit.text.toString().trim()
                if (newName.isNotEmpty() && newName != currentUsername) {
                    updateUsername(newName)
                }
                dlg.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUsername(newName: String) {
        lifecycleScope.launch {
            try {
                val r = api.updateUsername(bearerToken, UpdateUsernameRequest(newName))
                if (r.isSuccessful) {
                    r.body()?.let {
                        currentUsername = it.username ?: newName
                        binding.usernameTextView.text = currentUsername
                        Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${r.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
