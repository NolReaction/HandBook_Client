package com.example.kursach_handbook.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kursach_handbook.data.model.PlaceDto
import com.example.kursach_handbook.data.model.PlaceIdRequest
import com.example.kursach_handbook.data.remote.AuthApi
import com.example.kursach_handbook.data.remote.RetrofitProvider
import com.example.kursach_handbook.databinding.FragmentSearchBinding
import com.example.kursach_handbook.ui.login.adapters.PlaceAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray

class Search : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PlaceAdapter
    private lateinit var api: AuthApi

    // История
    private lateinit var historyAdapter: HistoryAdapter
    private var idleJob: Job? = null
    private val prefs by lazy {
        requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // API
        api = RetrofitProvider.createRetrofit(requireContext()).create(AuthApi::class.java)

        // Контентный список
        adapter = PlaceAdapter { place ->
            viewLifecycleOwner.lifecycleScope.launch {
                api.recordHistory(PlaceIdRequest(place.id))
            }
            // TODO: навигация в детали
        }
        binding.placesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.placesRecycler.adapter = adapter

        // История поиска (3 последних + футер "Очистить историю")
        historyAdapter = HistoryAdapter(
            onClickItem = { term ->
                binding.searchView.setQuery(term, false)
                adapter.filter(term)
                hideHistory()
                saveQuery(term) // поднимаем термин наверх
            },
            onDeleteItem = { term ->
                removeQuery(term)
                val left = loadHistory()
                if (left.isEmpty()) hideHistory() else historyAdapter.submit(left)
            },
            onClearAll = {
                clearHistory()
                hideHistory()
            }
        )
        binding.historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecycler.adapter = historyAdapter

        val searchView = binding.searchView

        // Показ/скрытие истории зависит от ФОКУСА
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (searchView.query.isEmpty()) scheduleHistoryIdle()
            } else {
                hideHistory()
                cancelIdleTimer()
            }
        }

        // Набор текста
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val q = query?.trim().orEmpty()
                if (q.isNotEmpty()) saveQuery(q)
                hideHistory()
                searchView.clearFocus()
                cancelIdleTimer()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                hideHistory()
                cancelIdleTimer()
                adapter.filter(newText)
                if (newText.isNullOrEmpty() && searchView.hasFocus()) {
                    scheduleHistoryIdle()
                }
                return true
            }
        })

        // Крестик SearchView
        searchView.setOnCloseListener {
            searchView.setQuery("", false)
            adapter.filter("")
            hideHistory()
            if (searchView.hasFocus()) scheduleHistoryIdle() else cancelIdleTimer()
            false
        }

        // Развернутое поле без авто-клавы
        searchView.apply {
            isIconified = false
            clearFocus()
        }

        // Любая активность в списках = скрыть историю и сбросить таймер
        binding.placesRecycler.setOnTouchListener { _, _ ->
            hideHistory(); cancelIdleTimer(); false
        }
        binding.historyRecycler.setOnTouchListener { _, _ ->
            cancelIdleTimer(); false
        }

        // Данные
        loadPlaces()
    }

    // ---------- idle таймер, завязанный на фокус SearchView ----------
    private fun scheduleHistoryIdle() {
        cancelIdleTimer()
        idleJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(2000) // 2 секунды
            if (isAdded && binding.searchView.hasFocus() && binding.searchView.query.isEmpty()) {
                showHistoryIfAny()
            }
        }
    }

    private fun cancelIdleTimer() {
        idleJob?.cancel()
        idleJob = null
    }

    private fun showHistoryIfAny() {
        val list = loadHistory()
        if (list.isEmpty()) return
        historyAdapter.submit(list)
        binding.historyRecycler.visibility = View.VISIBLE
        (binding.historyRecycler.layoutParams as? MarginLayoutParams)?.let {
            it.bottomMargin = dp(8)
            binding.historyRecycler.layoutParams = it
        }
    }

    private fun hideHistory() {
        binding.historyRecycler.visibility = View.GONE
    }

    private fun loadPlaces() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = api.getAll()
                if (resp.isSuccessful) {
                    val list: List<PlaceDto> = resp.body().orEmpty()
                    adapter.submitFullList(list)
                } else {
                    // TODO: показать ошибку
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: показать сетевую ошибку
            }
        }
    }

    // ---- История: хранение (макс 3) ----
    private fun loadHistory(): MutableList<String> {
        val raw = prefs.getString("history", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            val v = arr.optString(i).trim()
            if (v.isNotEmpty()) out += v
        }
        // страховка на случай старых данных > 3
        while (out.size > 3) out.removeAt(out.lastIndex)
        return out
    }

    private fun saveQuery(q: String) {
        val list = loadHistory()
        list.remove(q)   // убрать дубликат
        list.add(0, q)   // наверх
        while (list.size > 3) list.removeAt(list.lastIndex)
        prefs.edit { putString("history", JSONArray(list).toString()) }
    }

    private fun removeQuery(q: String) {
        val list = loadHistory()
        list.remove(q)
        prefs.edit { putString("history", JSONArray(list).toString()) }
    }

    private fun clearHistory() {
        prefs.edit { putString("history", "[]") }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/** Адаптер истории: элементы + футер "Очистить историю" */
/** Адаптер истории: элементы + футер "Очистить историю" */
/** Адаптер истории: элементы + футер "Очистить историю" */
private class HistoryAdapter(
    private val onClickItem: (String) -> Unit,
    private val onDeleteItem: (String) -> Unit,
    private val onClearAll: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_FOOTER = 1
    }

    private val items = mutableListOf<String>()

    fun submit(list: List<String>) {
        items.clear()
        items.addAll(list.take(3)) // максимум 3 подсказки
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size + if (items.isNotEmpty()) 1 else 0
    override fun getItemViewType(position: Int): Int =
        if (position < items.size) TYPE_ITEM else TYPE_FOOTER

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        fun Int.dp() = (this * parent.resources.displayMetrics.density).toInt()
        val padH = 12.dp()
        val padV = 4.dp()                 // было 6–12, стало 4dp = плотнее

        return if (viewType == TYPE_ITEM) {
            // Ряд: текст (вес=1) + мини-корзина справа
            val row = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(padH, padV, padH, padV)
                gravity = Gravity.CENTER_VERTICAL
                isClickable = true
            }

            val tv = TextView(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                )
                textSize = 15f
                includeFontPadding = false
                setPadding(0, 0, 0, 0)
                isSingleLine = true
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            val delSize = 18.dp()         // компактная иконка 18dp
            val del = ImageButton(parent.context).apply {
                layoutParams = LinearLayout.LayoutParams(delSize, delSize).apply {
                    marginStart = 8.dp()
                    gravity = Gravity.CENTER_VERTICAL // прижимаем к правому краю
                }
                setImageResource(android.R.drawable.ic_menu_delete)
                background = null
                setPadding(0, 0, 0, 0)
                minimumWidth = 0           // убираем дефолтные min размеры
                minimumHeight = 0
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                contentDescription = "Удалить из истории"
                isFocusable = true
                isClickable = true
            }

            row.addView(tv)
            row.addView(del)
            ItemVH(row, tv, del)
        } else {
            // Футер: "Очистить историю"
            val tv = TextView(parent.context).apply {
                setPadding(padH, padV, padH, padV)
                text = "Очистить историю"
                textSize = 14f
                includeFontPadding = false
                isClickable = true
            }
            FooterVH(tv)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemVH -> {
                holder.title.text = items[position]
                holder.itemView.setOnClickListener {
                    val pos = holder.adapterPosition
                    if (pos != RecyclerView.NO_POSITION) onClickItem(items[pos])
                }
                holder.delete.setOnClickListener {
                    val pos = holder.adapterPosition
                    if (pos != RecyclerView.NO_POSITION) onDeleteItem(items[pos])
                }
            }
            is FooterVH -> holder.view.setOnClickListener { onClearAll() }
        }
    }

    private class ItemVH(
        view: View,
        val title: TextView,
        val delete: ImageButton
    ) : RecyclerView.ViewHolder(view)

    private class FooterVH(val view: TextView) : RecyclerView.ViewHolder(view)
}


