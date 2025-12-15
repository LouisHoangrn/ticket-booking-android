package com.finalexam.project.model

/**
 * Data class đại diện cho phản hồi từ API thanh toán.
 * Giả sử API trả về trạng thái và một tin nhắn .
 */
data class PaymentResponse(
    val success: Boolean, // True nếu thanh toán thành công, False nếu thất bại
    val message: String, // Tin nhắn cho người dùng (ví dụ: "Thanh toán thành công" hoặc "Lỗi thẻ")
    val transactionId: String? // ID giao dịch nếu thành công
)

/**
 * Data class đại diện cho dữ liệu cần gửi đi khi thanh toán.
 * Trong ứng dụng thực tế, nó sẽ chứa thông tin giỏ hàng, ID người dùng, token thanh toán, v.v.
 * Ở đây ta chỉ dùng tổng tiền và ID người dùng.
 */
data class PaymentRequest(
    val userId: String,
    val totalAmount: Double,
    val items: List<CartItem>
)