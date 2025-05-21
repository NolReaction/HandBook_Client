package com.example.kursach_handbook.ui.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kursach_handbook.R
import com.example.kursach_handbook.data.model.HistoryEntryDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class HistoryAdapter : ListAdapter<HistoryEntryDto, HistoryAdapter.HVH>(Diff) {

    object Diff : DiffUtil.ItemCallback<HistoryEntryDto>() {
        override fun areItemsTheSame(old: HistoryEntryDto, new: HistoryEntryDto) =
            old.createdAt == new.createdAt && old.place.id == new.place.id

        override fun areContentsTheSame(old: HistoryEntryDto, new: HistoryEntryDto) =
            old == new
    }

    inner class HVH(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTv: TextView = view.findViewById(R.id.place_name)
        private val ratingBar: RatingBar = view.findViewById(R.id.place_rating)
        private val dateTv: TextView = view.findViewById(R.id.history_date)

        fun bind(item: HistoryEntryDto) {
            nameTv.text = item.place.name
            ratingBar.rating = item.place.rating

            // Парсим ISO-дату и форматируем в локальный
            val instant = Instant.parse(item.createdAt)
            val fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
                .withZone(ZoneId.systemDefault())
            dateTv.text = fmt.format(instant)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HVH(view)
    }

    override fun onBindViewHolder(holder: HVH, position: Int) {
        holder.bind(getItem(position))
    }
}
