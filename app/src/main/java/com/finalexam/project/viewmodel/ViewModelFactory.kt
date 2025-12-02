package com.finalexam.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.finalexam.project.repository.AuthRepository

class ViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Kiểm tra nếu ViewModel được yêu cầu là AuthViewModel
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            // Khởi tạo AuthViewModel với AuthRepository đã truyền vào
            return AuthViewModel(repository) as T
        }

        // Bạn sẽ thêm các ViewModel khác ở đây khi tạo chúng (ví dụ: HomeViewModel)

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}