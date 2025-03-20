package com.example.kursach_handbook.ui.authorization

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kursach_handbook.data.model.ForgotPasswordRequest
import com.example.kursach_handbook.data.model.LoginRequest
import com.example.kursach_handbook.data.model.LoginResponse
import com.example.kursach_handbook.data.model.RegisterRequest
import com.example.kursach_handbook.data.model.RegisterResponse
import com.example.kursach_handbook.data.remote.AuthApi
import com.example.kursach_handbook.data.remote.RetrofitProvider
import com.example.kursach_handbook.data.token.TokenManager
import kotlinx.coroutines.launch

// Создаем sealed-класс для событий
sealed class AuthEvent {
    data class ShowError(val message: String) : AuthEvent()
    object LoginSuccess : AuthEvent()
    object RegisterSuccess : AuthEvent()
    object ForgotPasswordSuccess : AuthEvent()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    // Создаем authApi через RetrofitProvider, используя application context
    private val authApi: AuthApi = RetrofitProvider.createRetrofit(getApplication()).create(AuthApi::class.java)

    // LiveData для отслеживания результата авторизации
    private val _loginResult = MutableLiveData<LoginResponse?>()
    val loginResult: LiveData<LoginResponse?> get() = _loginResult

    // LiveData для отслеживания результата регистрации
    private val _registerResult = MutableLiveData<RegisterResponse?>()
    val registerResult: LiveData<RegisterResponse?> get() = _registerResult

    // LiveData для событий
    private val _authEvent = MutableLiveData<AuthEvent>()
    val authEvent: LiveData<AuthEvent> get() = _authEvent

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Выполняем запрос авторизации через authApi
                val response = authApi.login(LoginRequest(email, password))
                val loginResponse = response.body()
                if (response.isSuccessful && loginResponse != null) {
                    _loginResult.value = loginResponse
                    _authEvent.value = AuthEvent.LoginSuccess
                    // Сохраняем токен
                    TokenManager.saveAuthData(getApplication(),  loginResponse.token, loginResponse.is_verified)
                } else {
                    _loginResult.value = null
                    _authEvent.value = AuthEvent.ShowError("Incorrect email or password")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _loginResult.value = null
                _authEvent.value = AuthEvent.ShowError("Network error")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Выполняем запрос регистрации через authApi
                val response = authApi.register(RegisterRequest(email, password))
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse != null) {
                        _registerResult.value = registerResponse
                        _authEvent.value = AuthEvent.RegisterSuccess
                        TokenManager.saveAuthData(getApplication(), registerResponse.token, registerResponse.is_verified)
                    } else {
                        _registerResult.value = null
                        _authEvent.value = AuthEvent.ShowError("Registration failed")
                    }
                } else {
                    _registerResult.value = null
                    if (response.code() == 409) {
                        // Если код ошибки 409, значит пользователь уже существует
                        _authEvent.value = AuthEvent.ShowError("User already exists")
                    } else {
                        _authEvent.value = AuthEvent.ShowError("?Registration failed?")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _registerResult.value = null
                _authEvent.value = AuthEvent.ShowError("Network error")
            }
        }
    }

    fun recover(email: String) {
        viewModelScope.launch {
            try {
                val response = authApi.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) {
                    _authEvent.value = AuthEvent.ForgotPasswordSuccess
                } else {
                    _authEvent.value = AuthEvent.ShowError("Failed to send reset link")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authEvent.value = AuthEvent.ShowError("Network error")
            }
        }
    }
}
