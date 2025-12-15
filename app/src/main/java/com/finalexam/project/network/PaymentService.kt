package com.finalexam.project.network

import com.finalexam.project.model.PaymentRequest
import com.finalexam.project.model.PaymentResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Định nghĩa giao diện cho các cuộc gọi API liên quan đến Thanh toán.
 */
interface PaymentService {

    /**
     * Phương thức POST giả lập để xử lý thanh toán.
     * Trong một ứng dụng thực tế, đây sẽ là đường dẫn đến cổng thanh toán của bạn.
     *
     * @param request Dữ liệu thanh toán bao gồm giỏ hàng và tổng tiền.
     * @return Kết quả phản hồi thanh toán.
     */
    @POST("api/checkout")
    suspend fun checkout(@Body request: PaymentRequest): PaymentResponse
}


// Singleton để cung cấp đối tượng Retrofit và PaymentService
object PaymentApi {
    // URL cơ sở (chỉ là giả lập, trong thực tế phải là server thật)
    private const val BASE_URL = "https://mock.api.cinema/"

    // Khởi tạo Retrofit
    private val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()

    /**
     * Đối tượng giả lập để mô phỏng phản hồi API.
     * Thay vì gọi Retrofit.create(PaymentService::class.java),
     * ta sử dụng một Mock Implementation để tránh lỗi mạng.
     */
    val paymentService: PaymentService = object : PaymentService {
        // Biến này được sử dụng để mô phỏng một lần thành công, một lần thất bại
        private var isSuccessNext = true

        override suspend fun checkout(request: PaymentRequest): PaymentResponse {
            // Mô phỏng độ trễ mạng
            kotlinx.coroutines.delay(2000)

            // Logic giả lập: Tổng tiền chẵn -> thành công, Tổng tiền lẻ -> thất bại
            // Hoặc luân phiên thành công/thất bại
            val mockSuccess = isSuccessNext
            isSuccessNext = !isSuccessNext // Đảo ngược trạng thái cho lần gọi tiếp theo

            return if (mockSuccess) {
                PaymentResponse(
                    success = true,
                    message = "Thanh toán thành công. Cảm ơn bạn đã đặt vé!",
                    transactionId = "TXN-${System.currentTimeMillis()}"
                )
            } else {
                PaymentResponse(
                    success = false,
                    message = "Thanh toán thất bại. Vui lòng kiểm tra lại thông tin thẻ.",
                    transactionId = null
                )
            }
        }
    }
}