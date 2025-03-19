package com.example.kursach_handbook.data.remote

import com.example.kursach_handbook.data.model.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface AuthApi {
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<RegisterResponse>
}
