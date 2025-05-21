// ui/login/PlaceAdapter.kt
package com.example.kursach_handbook.ui.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kursach_handbook.R
import com.example.kursach_handbook.data.model.PlaceDto
import java.util.*

class PlaceAdapter(
    private val onClick: (PlaceDto) -> Unit
) : ListAdapter<PlaceDto, PlaceAdapter.PlaceVH>(PlaceDiffCallback) {

    // текущий полный список (для фильтрации)
    private var originalList: List<PlaceDto> = emptyList()

    inner class PlaceVH(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTv: TextView = view.findViewById(R.id.place_name)
        private val ratingBar: RatingBar = view.findViewById(R.id.place_rating)

        fun bind(place: PlaceDto) {
            nameTv.text = place.name
            ratingBar.rating = place.rating
            itemView.setOnClickListener { onClick(place) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceVH(view)
    }

    override fun onBindViewHolder(holder: PlaceVH, position: Int) {
        holder.bind(getItem(position))
    }

    /** Передаём полный список и сразу отображаем его */
    fun submitFullList(list: List<PlaceDto>) {
        originalList = list
        submitList(list)  // ListAdapter сам вызовет diff и обновит RecyclerView
    }

    /** Фильтруем оригинальный список и подаём в ListAdapter */
    fun filter(query: String?) {
        val q = query
            ?.lowercase(Locale("ru"))
            ?.trim()
            ?: ""
        val filtered = if (q.isEmpty()) {
            originalList
        } else {
            originalList.filter { place ->
                place.name.lowercase(Locale("ru")).contains(q)
            }
        }
        submitList(filtered)  // тоже вызовет DiffUtil и обновит только изменившиеся элементы
    }
}
