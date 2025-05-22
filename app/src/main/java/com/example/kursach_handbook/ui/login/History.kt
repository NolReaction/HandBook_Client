package com.example.kursach_handbook.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kursach_handbook.data.remote.AuthApi
import com.example.kursach_handbook.data.remote.RetrofitProvider
import com.example.kursach_handbook.databinding.FragmentHistoryBinding
import com.example.kursach_handbook.data.model.HistoryEntryDto
import com.example.kursach_handbook.ui.login.adapters.HistoryAdapter
import kotlinx.coroutines.launch

class History : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HistoryAdapter
    private lateinit var api: AuthApi

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?) : View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, saved: Bundle?) {
        super.onViewCreated(view, saved)

        api = RetrofitProvider
            .createRetrofit(requireContext())
            .create(AuthApi::class.java)

        adapter = HistoryAdapter()
        binding.historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecycler.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            try {
                val resp = api.getHistory()
                if (resp.isSuccessful) {
                    val list: List<HistoryEntryDto> = resp.body().orEmpty()
                    adapter.submitList(list)
                } else {
                    // TODO: показать ошибку
                }
            } catch (e: Exception) {
                // TODO: показать сетевую ошибку
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
