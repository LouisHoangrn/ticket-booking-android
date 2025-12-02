package com.finalexam.project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.finalexam.project.data.Result
import com.finalexam.project.repository.AuthRepository
import com.google.firebase.auth.AuthResult

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Gọi Repository để thực hiện đăng nhập và trả về LiveData
    fun login(email: String, password: String): LiveData<Result<AuthResult>> {
        return repository.login(email, password)
    }

    // Gọi Repository để thực hiện đăng ký và trả về LiveData
    fun register(email: String, password: String): LiveData<Result<AuthResult>> {
        return repository.register(email, password)
    }

    // Kiểm tra trạng thái đăng nhập
    fun isAuthenticated(): Boolean {
        return repository.getCurrentUser() != null
    }
}