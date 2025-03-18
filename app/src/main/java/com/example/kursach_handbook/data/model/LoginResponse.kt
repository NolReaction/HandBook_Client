package com.example.kursach_handbook.data.model

data class LoginResponse(
    val token: String,
    val userId: Int,
    val userName: String
)
