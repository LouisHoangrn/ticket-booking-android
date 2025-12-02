package com.finalexam.project.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.finalexam.project.data.Result
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Hàm Đăng ký (Register)
    fun register(email: String, password: String): LiveData<Result<AuthResult>> {
        // Bắt đầu với trạng thái Loading
        val resultLiveData = MutableLiveData<Result<AuthResult>>(Result.Loading)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // Đăng ký thành công
                resultLiveData.postValue(Result.Success(authResult))
            }
            .addOnFailureListener { exception ->
                // Đăng ký thất bại
                resultLiveData.postValue(Result.Error(exception))
            }

        return resultLiveData
    }

    // Hàm Đăng nhập (Login)
    fun login(email: String, password: String): LiveData<Result<AuthResult>> {
        // Bắt đầu với trạng thái Loading
        val resultLiveData = MutableLiveData<Result<AuthResult>>(Result.Loading)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // Đăng nhập thành công
                resultLiveData.postValue(Result.Success(authResult))
            }
            .addOnFailureListener { exception ->
                // Đăng nhập thất bại
                resultLiveData.postValue(Result.Error(exception))
            }

        return resultLiveData
    }

    // Hàm kiểm tra trạng thái đăng nhập
    fun getCurrentUser() = auth.currentUser
}