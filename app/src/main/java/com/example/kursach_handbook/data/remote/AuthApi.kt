package com.example.kursach_handbook.data.remote

import com.example.kursach_handbook.data.model.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.PATCH

interface AuthApi {
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<RegisterResponse>

    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") authToken: String
    ): Response<UserDto>

    @PATCH("profile/username")
    suspend fun updateUsername(
        @Header("Authorization") token: String,
        @Body req: UpdateUsernameRequest
    ): Response<UserDto>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordMessageResponse>

    @GET("places")
    suspend fun getAll(): Response<List<PlaceDto>>

    @POST("history")
    suspend fun recordHistory(@Body req: PlaceIdRequest): Response<Unit>

    @GET("history")
    suspend fun getHistory(): Response<List<HistoryEntryDto>>
}
