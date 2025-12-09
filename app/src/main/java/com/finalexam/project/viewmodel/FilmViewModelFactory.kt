package com.finalexam.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.finalexam.project.repository.BookmarkRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth

/**
 * Factory để tạo FilmViewModel và inject BookmarkRepository cùng các dependencies ngu.
 */
class FilmViewModelFactory(
    private val database: FirebaseDatabase,
    private val userId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FilmViewModel::class.java)) {
            val repository = BookmarkRepository(database, userId)
            @Suppress("UNCHECKED_CAST")
            return FilmViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}