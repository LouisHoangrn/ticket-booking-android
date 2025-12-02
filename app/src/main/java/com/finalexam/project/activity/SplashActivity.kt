package com.finalexam.project.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.finalexam.project.databinding.ActivitySplashBinding
import com.finalexam.project.repository.AuthRepository
import com.finalexam.project.viewmodel.AuthViewModel
import com.finalexam.project.viewmodel.ViewModelFactory

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var authViewModel: AuthViewModel // Sử dụng AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo ViewModel
        val factory = ViewModelFactory(AuthRepository())
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        binding.startBtn.setOnClickListener {
            checkAuthAndNavigate() // Chuyển màn hình khi người dùng click
        }
    }

    // Hàm kiểm tra trạng thái đăng nhập và điều hướng
    private fun checkAuthAndNavigate() {
        if (authViewModel.isAuthenticated()) {
            // Đã đăng nhập: Chuyển thẳng đến màn hình chính
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Chưa đăng nhập: Chuyển đến màn hình đăng nhập
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}