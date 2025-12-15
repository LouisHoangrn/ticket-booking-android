package com.finalexam.project.model

/**
 * Model đại diện cho một gói vé được thêm vào giỏ hàng.
 * Sử dụng cho Firebase Realtime Database.
 */
data class CartItem(
    // Tên thuộc tính: cartItemId, filmTitle , filmPrice, quantity, totalPrice
    var cartItemId: String? = null, // Key duy nhất từ Firebase
    val filmId: Int = 0,
    val filmTitle: String = "",
    val filmPrice: Double = 0.0, // Giá 1 vé
    val selectedDate: String = "",
    val selectedTime: String = "",
    val seatNames: List<String> = emptyList(), // Danh sách tên ghế (ví dụ: ["A1", "A2"])
    val quantity: Int = 0, // Số lượng vé
    // Thuộc tính totalPrice: Nên tính toán để tránh sai sót, nhưng giữ lại để đồng bộ với Firebase
    val totalPrice: Double = 0.0, // Tổng giá = filmPrice * quantity
    val timestamp: Long = System.currentTimeMillis() // Thời điểm thêm vào giỏ hàng
)