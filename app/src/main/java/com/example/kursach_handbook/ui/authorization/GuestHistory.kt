package com.example.kursach_handbook.ui.authorization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kursach_handbook.R
import com.example.kursach_handbook.databinding.FragmentGuestHistoryBinding
import com.example.kursach_handbook.databinding.FragmentGuestProfileBinding


class GuestHistory : Fragment() {
    private var _binding: FragmentGuestHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuestHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.goToProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_guestHistory_item_to_guestProfile_item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}