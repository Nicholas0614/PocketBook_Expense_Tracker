package com.example.pocketbook_expensetracker.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketbook_expensetracker.data.repo.AuthRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repo = AuthRepo.getInstance()

    private val _authState = MutableStateFlow<String?>(null)
    val authState: StateFlow<String?> = _authState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = "Please enter email and password"
            return
        }

        viewModelScope.launch {
            _authState.value = "Loading"
            val result = repo.login(email.trim(), password)
            _authState.value = if (result.isSuccess) "Success" else result.exceptionOrNull()?.message ?: "Login failed"
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = "Please fill in all fields"
            return
        }

        viewModelScope.launch {
            _authState.value = "Loading"
            val result = repo.register(name.trim(), email.trim(), password)
            _authState.value = if (result.isSuccess) "Success" else result.exceptionOrNull()?.message ?: "Registration failed"
        }
    }


    fun isLoggedIn(): Boolean = repo.isLoggedIn()

    fun logout() {
        repo.logout()
        clearState()
    }


    fun clearState() {
        _authState.value = null
    }
}
