package com.finalexam.project.network

import com.finalexam.project.model.CartItem
import com.finalexam.project.model.PaymentRequest
import com.finalexam.project.model.PaymentResponse
import kotlinx.coroutines.delay
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Định nghĩa giao diện cho các cuộc gọi API liên quan đến Thanh toán.
 */
interface PaymentService {

    /**
     * Phương thức POST giả lập để xử lý thanh toán.
     *
     * @param request Dữ liệu thanh toán bao gồm giỏ hàng và tổng tiền.
     * @return Kết quả phản hồi thanh toán .
     */
    @POST("api/checkout")
    suspend fun checkout(@Body request: PaymentRequest): PaymentResponse
}


/**
 * Singleton để cung cấp đối tượng Retrofit và PaymentService.
 * Sử dụng Mock Implementation để mô phỏng API.
 */
object PaymentApi {
    // URL cơ sở (chỉ là giả lập)
    private const val BASE_URL = "https://mock.api.cinema/"

    // Khởi tạo Retrofit (để giữ cấu trúc cho API thật trong tương lai)
    private val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()

    /**
     * Đối tượng giả lập để mô phỏng phản hồi API với logic kiểm tra trùng lặp.
     */
    val paymentService: PaymentService = object : PaymentService {

        override suspend fun checkout(request: PaymentRequest): PaymentResponse {
            // Mô phỏng độ trễ mạng
            delay(2000)

            // Bước 1: Flatten giỏ hàng thành một danh sách các "Booking Key" (Ghế đơn lẻ)
            // Booking Key: (FilmId, SelectedDate, SelectedTime, SeatName)
            val bookingKeys = request.items.flatMap { item ->
                item.seatNames.map { seatName ->
                    // Tạo một key composite duy nhất: ID Phim_Ngày_Giờ_Ghế
                    // Dựa trên các thuộc tính của CartItem bạn đã cung cấp: filmId, selectedDate, selectedTime, seatNames
                    "${item.filmId}_${item.selectedDate}_${item.selectedTime}_$seatName"
                }
            }

            // Bước 2: Kiểm tra trùng lặp trong danh sách Booking Key đã làm phẳng
            val duplicateKeys = bookingKeys
                .groupBy { it } // Gom nhóm các key giống nhau
                .filter { it.value.size > 1 } // Lọc ra những nhóm có số lượng > 1

            val hasDuplicateBooking = duplicateKeys.isNotEmpty()

            return if (hasDuplicateBooking) {
                // THẤT BẠI: Nếu phát hiện bất kỳ ghế nào bị đặt trùng trong cùng một suất chiếu
                PaymentResponse(
                    success = false,
                    message = "Lỗi Thanh Toán: Đã phát hiện ghế trùng lặp. Vui lòng kiểm tra lại giỏ hàng vì một số ghế đã được chọn nhiều lần cho cùng một suất chiếu (Cùng Phim, Ngày, Giờ).",
                    transactionId = null
                )
            } else {
                // THÀNH CÔNG: Nếu không có ghế nào bị đặt trùng
                PaymentResponse(
                    success = true,
                    message = "Thanh toán thành công. Cảm ơn bạn đã đặt vé!",
                    transactionId = "TXN-${System.currentTimeMillis()}"
                )
            }
        }
    }
}