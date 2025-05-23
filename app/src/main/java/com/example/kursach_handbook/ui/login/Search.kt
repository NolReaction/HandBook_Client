package com.example.kursach_handbook.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kursach_handbook.data.model.PlaceDto
import com.example.kursach_handbook.data.model.PlaceIdRequest
import com.example.kursach_handbook.data.remote.AuthApi
import com.example.kursach_handbook.data.remote.RetrofitProvider
import com.example.kursach_handbook.databinding.FragmentSearchBinding
import com.example.kursach_handbook.ui.login.adapters.PlaceAdapter
import kotlinx.coroutines.launch

class Search : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PlaceAdapter
    private lateinit var api: AuthApi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Создаём Retrofit API
        api = RetrofitProvider
            .createRetrofit(requireContext())
            .create(AuthApi::class.java)

        adapter = PlaceAdapter { place ->
            // 1) сразу отправляем историю
            lifecycleScope.launch {
                api.recordHistory(PlaceIdRequest(place.id))
            }
            // 2) навигация в детали
            // ...
        }

        binding.placesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.placesRecycler.adapter = adapter

        // Слушаем поисковую строку
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText)
                return true
            }
        })

        // Принудительно развернуть и снять фокус
        binding.searchView.apply {
            isIconified = false    // разворачиваем
            clearFocus()           // снимаем фокус (если не хотим сразу показывать клавиатуру)
        }

        // Грузим данные
        loadPlaces()
    }

    private fun loadPlaces() {
        lifecycleScope.launch {
            try {
                val resp = api.getAll()
                if (resp.isSuccessful) {
                    val list: List<PlaceDto> = resp.body().orEmpty()
                    adapter.submitFullList(list)
                } else {
                    // TODO: показать ошибку, например Toast
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: показать сетевую ошибку
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
