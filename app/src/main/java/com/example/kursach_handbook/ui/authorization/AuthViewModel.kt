package com.example.kursach_handbook.ui.authorization

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kursach_handbook.data.model.LoginRequest
import com.example.kursach_handbook.data.model.LoginResponse
import com.example.kursach_handbook.data.remote.AuthApi
import com.example.kursach_handbook.data.remote.RetrofitProvider
import com.example.kursach_handbook.data.token.TokenManager
import kotlinx.coroutines.launch

// Создаем sealed-класс для событий
sealed class AuthEvent {
    data class ShowError(val message: String) : AuthEvent()
    object LoginSuccess : AuthEvent()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    // Создаем authApi через RetrofitProvider, используя application context
    private val authApi: AuthApi = RetrofitProvider.createRetrofit(getApplication()).create(AuthApi::class.java)

    // LiveData для отслеживания результата авторизации
    private val _loginResult = MutableLiveData<LoginResponse?>()
    val loginResult: LiveData<LoginResponse?> get() = _loginResult

    // LiveData для событий
    private val _authEvent = MutableLiveData<AuthEvent>()
    val authEvent: LiveData<AuthEvent> get() = _authEvent

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Выполняем запрос авторизации через созданный authApi
                val response = authApi.login(LoginRequest(email, password))
                val loginResponse = response.body()
                if (response.isSuccessful && loginResponse != null) {
                    _loginResult.value = loginResponse
                    _authEvent.value = AuthEvent.LoginSuccess
                    // Сохраняем токен
                    TokenManager().saveToken(getApplication(), loginResponse.token)
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

}
