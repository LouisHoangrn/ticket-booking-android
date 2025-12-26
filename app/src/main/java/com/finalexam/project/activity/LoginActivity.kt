package com.finalexam.project.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.finalexam.project.data.Result // Import lớp Result đã tạo
import com.finalexam.project.databinding.ActivityLoginBinding
import com.finalexam.project.repository.AuthRepository
import com.finalexam.project.viewmodel.AuthViewModel
import com.finalexam.project.viewmodel.ViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: AuthViewModel // Khai báo ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo ViewModel
        val factory = ViewModelFactory(AuthRepository())
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val pass = binding.edtPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show()
            } else {
                // Gọi hàm Đăng nhập qua ViewModel
                loginUser(email, pass)
            }
        }

        binding.btnToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        // Ẩn/Hiển thị ProgressBar (Tùy chọn)
        // binding.progressBar.visibility = View.VISIBLE

        authViewModel.login(email, password).observe(this) { result ->
            // binding.progressBar.visibility = View.GONE // Tắt loading
            when (result) {
                is Result.Loading -> {
                    // Đang tải
                    Toast.makeText(this, "Đang xử lý đăng nhập...", Toast.LENGTH_SHORT).show()
                }
                is Result.Success -> {
                    // Đăng nhập thành công! Chuyển màn hình
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is Result.Error -> {
                    // Tùy chỉnh thông báo, bỏ qua thông báo chi tiết của Firebase
                    val customMessage = "Tài khoản hoặc mật khẩu không đúng. Vui lòng kiểm tra lại!"

                    Toast.makeText(this, "Đăng nhập thất bại: $customMessage", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}