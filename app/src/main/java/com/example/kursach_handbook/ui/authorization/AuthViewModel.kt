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
                val response = authApi.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        _loginResult.value = loginResponse
                        _authEvent.value = AuthEvent.LoginSuccess
                        TokenManager.saveAuthData(getApplication(), loginResponse.token, loginResponse.is_verified)
                    } else {
                        _loginResult.value = null
                        _authEvent.value = AuthEvent.ShowError("Неожиданная ошибка")
                    }
                } else {
                    // Если получен код 429, попробуем извлечь сообщение из errorBody
                    if (response.code() == 429) {
                        val errorMsg = response.errorBody()?.string() ?: "Слишком много неудачных попыток. Попробуйте ещё раз позже."
                        _authEvent.value = AuthEvent.ShowError(errorMsg)
                    } else {
                        _authEvent.value = AuthEvent.ShowError("Неверный адрес электронной почты или пароль")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _loginResult.value = null
                _authEvent.value = AuthEvent.ShowError("Ошибка сети")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = authApi.register(RegisterRequest(email, password))
                if (response.isSuccessful) {
                    response.body()?.let { resp ->
                        _registerResult.value = resp
                        _authEvent.value    = AuthEvent.RegisterSuccess
                        TokenManager.saveAuthData(
                            getApplication(),
                            resp.token,
                            resp.is_verified
                        )
                    } ?: run {
                        _registerResult.value = null
                        _authEvent.value      = AuthEvent.ShowError("Неожиданный ответ сервера")
                    }
                } else {
                    // Разбираем конкретный код и тело ответа
                    val errorBody = response.errorBody()?.string().orEmpty()
                    val userMessage = when (response.code()) {
                        400 -> errorBody.takeIf { it.isNotBlank() }
                            ?: "Неверный формат или почты не существует"
                        409 -> "Пользователь с таким e-mail уже зарегистрирован"
                        else -> "Ошибка регистрации: (${response.code()})"
                    }
                    _registerResult.value = null
                    _authEvent.value      = AuthEvent.ShowError(userMessage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _registerResult.value = null
                _authEvent.value      = AuthEvent.ShowError("Ошибка соединения")
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
                    _authEvent.value = AuthEvent.ShowError("Не удалось отправить ссылку для сброса")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authEvent.value = AuthEvent.ShowError("Ошибка сети")
            }
        }
    }
}
