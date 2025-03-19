package com.example.kursach_handbook.data.model

data class RegisterResponse(
    val token: String,
    val userId: Int,
    val userEmail: String,
    val is_verified: Boolean
)