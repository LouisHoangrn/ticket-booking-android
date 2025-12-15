package com.finalexam.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.FirebaseDatabase

/**
 * F actory để khởi tạo CartViewModel với các tham số yêu cầu (Database và UserId).
 */
class CartViewModelFactory(
    private val database: FirebaseDatabase,
    private val userId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            // Khởi tạo CartViewModel với FirebaseDatabase và userId
            return CartViewModel(database, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}