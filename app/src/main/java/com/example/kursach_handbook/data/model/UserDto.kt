package com.example.kursach_handbook.data.model

data class UserDto(
    val id: Int,
    val email: String,
    val username: String,
    val avatar: String,
    val is_verified: Boolean
)