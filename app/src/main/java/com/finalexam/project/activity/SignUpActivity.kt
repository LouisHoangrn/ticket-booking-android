package com.finalexam.project.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.finalexam.project.data.Result // Import lớp Result đã tạo
import com.finalexam.project.databinding.ActivitySignupBinding
import com.finalexam.project.repository.AuthRepository
import com.finalexam.project.viewmodel.AuthViewModel
import com.finalexam.project.viewmodel.ViewModelFactory

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var authViewModel: AuthViewModel // Khai báo ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo ViewModel
        val factory = ViewModelFactory(AuthRepository())
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        binding.btnSignUp.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val pass = binding.edtPassword.text.toString().trim()
            val confirm = binding.edtConfirmPassword.text.toString().trim()

            when {
                email.isEmpty() || pass.isEmpty() || confirm.isEmpty() -> {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                }
                pass != confirm -> {
                    Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                }
                pass.length < 6 -> {
                    Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Gọi hàm đăng ký qua ViewModel
                    registerUser(email, pass)
                }
            }
        }

        binding.btnToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(email: String, password: String) {
        // Ẩn/Hiển thị ProgressBar (Tùy chọn: cần thêm ProgressBar vào XML)
        // binding.progressBar.visibility = View.VISIBLE

        authViewModel.register(email, password).observe(this) { result ->
            // binding.progressBar.visibility = View.GONE // Tắt loading khi có kết quả
            when (result) {
                is Result.Loading -> {
                    // Đang tải, có thể hiện ProgressBar
                    Toast.makeText(this, "Đang xử lý đăng ký...", Toast.LENGTH_SHORT).show()
                }
                is Result.Success -> {
                    // Đăng ký thành công
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                is Result.Error -> {
                    // Tùy chỉnh thông báo, bỏ qua thông báo chi tiết của Firebase
                    val customMessage = "Lỗi đăng ký không xác định, Email bạn đang dùng đã tồn tại hoặc có lỗi kết nối mạng!"

                    Toast.makeText(this, "Đăng ký thất bại: $customMessage", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}