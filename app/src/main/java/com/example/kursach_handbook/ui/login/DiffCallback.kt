package com.example.kursach_handbook.ui.login

import androidx.recyclerview.widget.DiffUtil
import com.example.kursach_handbook.data.model.PlaceDto

object PlaceDiffCallback : DiffUtil.ItemCallback<PlaceDto>() {
    override fun areItemsTheSame(old: PlaceDto, new: PlaceDto): Boolean {
        // считаем, что одно и то же место, если совпадают id
        return old.id == new.id
    }
    override fun areContentsTheSame(old: PlaceDto, new: PlaceDto): Boolean {
        // проверяем все поля (или только те, что влияют на отображение)
        return old == new
    }
}