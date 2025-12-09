package com.finalexam.project.model

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

/**
 * Data class đại diện cho một mục vé trong Giỏ hàng (Cart).
 * Chứa thông tin chi tiết về phim, suất chiếu, và số lượng/ghế.
 */
@IgnoreExtraProperties
data class CartItem(
    var filmId: String? = null,
    var filmTitle: String? = null,
    var filmPoster: String? = null,
    var showDate: String? = null,
    var showTime: String? = null,
    // Giá tiền cho MỘT vé
    var ticketPrice: Double = 0.0,
    // Số lượng vé đã chọn
    var quantity: Int = 1,
    // Danh sách các ghế đã chọn (được lưu dưới dạng String hoặc List<String> tùy cách bạn triển khai)
    var selectedSeats: String? = null
) : Serializable
{
    // Constructor không tham số (BẮT BUỘC cho Firebase Realtime Database)
    constructor() : this(
        null, null, null, null, null,
        0.0, 1, null
    )

    // Tính tổng tiền của riêng mục này
    val totalPrice: Double
        get() = ticketPrice * quantity
}