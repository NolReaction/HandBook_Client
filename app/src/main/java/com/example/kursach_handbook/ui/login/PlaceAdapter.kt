package com.example.kursach_handbook.ui.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kursach_handbook.R
import com.example.kursach_handbook.data.model.PlaceDto
import java.util.*

class PlaceAdapter(
    private var items: List<PlaceDto>,
    private val onClick: (PlaceDto) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceVH>() {

    // текущий отображаемый список (для фильтрации)
    private var filtered = items.toList()

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

    override fun getItemCount(): Int = filtered.size

    override fun onBindViewHolder(holder: PlaceVH, position: Int) {
        holder.bind(filtered[position])
    }

    /** Обновить весь список */
    fun submitList(newList: List<PlaceDto>) {
        items = newList
        filtered = newList
        notifyDataSetChanged()
    }

    /** Отфильтровать по названию */
    fun filter(query: String?) {
        val q = query
            ?.lowercase(Locale("ru"))
            ?.trim()
            ?: ""
        filtered = if (q.isEmpty()) {
            items
        } else {
            items.filter { place ->
                // сравниваем в нижнем регистре, с учётом русской локали:
                place.name.lowercase(Locale("ru")).contains(q)
            }
        }
        notifyDataSetChanged()
    }
}
