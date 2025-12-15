package com.finalexam.project.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finalexam.project.model.CartItem
import com.finalexam.project.model.PaymentRequest
import com.finalexam.project.model.PaymentResponse
import com.finalexam.project.network.PaymentApi
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Định nghĩa trạng thái cho hoạt động Thanh toán
sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    data class Success(val response: PaymentResponse) : PaymentState()
    data class Error(val message: String) : PaymentState()
}

/**
 * ViewModel xử lý logic Giỏ hàng và Thanh toán.
 * * Lưu ý: ViewModel này cần FirebaseDatabase và userId.
 * Trong ứng dụng thực tế, bạn sẽ cần cơ chế Dependency Injection
 * để cung cấp các đối tượng này.
 */
class CartViewModel(
    // Giả định bạ n có thể truyền instance FirebaseDatabase
    database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    // Giả định bạn có thể truyền userId của người dùng hiện tại
    private val userId: String = "user-test-id"
) : ViewModel() {

    private val cartRef: DatabaseReference = database.getReference("carts").child(userId)

    // LiveData cho danh sách item trong  giỏ hàng
    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    // LiveData cho tổng giá trị
    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    // --- STATE FLOW CHO THANH TOÁN ---
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState
    // ------------------------------------

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            // Đảm bảo CartItem có constructor không đối số để Firebase hoạt động
            val items = snapshot.children.mapNotNull { it.getValue<CartItem>() }
            _cartItems.value = items
            calculateTotalPrice(items)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("CartViewModel", "Lỗi đọc dữ liệu Firebase: ${error.message}")
        }
    }

    init {
        // Bắt đầu lắng nghe thay đổi dữ liệu khi ViewModel được tạo
        cartRef.addValueEventListener(valueEventListener)
    }

    /**
     * Tính toán tổng giá trị của tất cả các item trong giỏ hàng.
     * Sử dụng thuộc tính 'totalPrice' của CartItem (tổng giá cho gói vé đó).
     */
    private fun calculateTotalPrice(items: List<CartItem>) {
        // Tính tổng bằng cách cộng thuộc tính totalPrice (đã được tính: filmPrice * quantity) của từng item
        val total = items.sumOf { it.totalPrice }
        _totalPrice.value = total
    }

    /**
     * Xóa một item khỏi giỏ hàng.
     */
    fun removeItem(cartItemId: String) {
        cartRef.child(cartItemId).removeValue()
            .addOnFailureListener { e ->
                Log.e("CartViewModel", "Lỗi xóa item: ${e.message}")
            }
    }

    /**
     * Thêm một item mới vào giỏ hàng.
     * (Giả lập: Trong thực tế, bạn sẽ cần logic để tạo CartItem với totalPrice đúng)
     */
    fun addItem(item: CartItem) {
        // Tính toán totalPrice trước khi lưu (phòng trường hợp CartItem được tạo ở client)
        val calculatedItem = item.copy(totalPrice = item.filmPrice * item.quantity)

        // Tạo key mới nếu chưa có
        val itemId = calculatedItem.cartItemId ?: cartRef.push().key ?: return

        cartRef.child(itemId).setValue(calculatedItem.copy(cartItemId = itemId))
            .addOnFailureListener { e ->
                Log.e("CartViewModel", "Lỗi thêm item: ${e.message}")
            }
    }

    /**
     * --- CHỨC NĂNG THANH TOÁN ---
     * Mô phỏng quá trình gọi API thanh toán.
     */
    fun checkout() {
        if (_totalPrice.value == 0.0 || _cartItems.value.isNullOrEmpty()) {
            _paymentState.update { PaymentState.Error("Giỏ hàng trống. Vui lòng thêm vé.") }
            return
        }

        // Ngăn chặn các cuộc gọi API trùng lặp
        if (_paymentState.value is PaymentState.Loading) return

        _paymentState.update { PaymentState.Loading }

        val request = PaymentRequest(
            userId = userId,
            totalAmount = _totalPrice.value ?: 0.0,
            items = _cartItems.value.orEmpty()
        )

        viewModelScope.launch {
            try {
                // Gọi API Giả lập (sử dụng PaymentApi.paymentService đã được tạo mock)
                val response = PaymentApi.paymentService.checkout(request)

                if (response.success) {
                    _paymentState.update { PaymentState.Success(response) }
                    // Xóa giỏ hàng sau khi thanh toán thành công
                    clearCart()
                } else {
                    _paymentState.update { PaymentState.Error(response.message) }
                }

            } catch (e: Exception) {
                Log.e("CartViewModel", "Lỗi API Thanh toán: ${e.message}", e)
                _paymentState.update { PaymentState.Error("Lỗi kết nối mạng hoặc lỗi máy chủ.") }
            }
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng (sau khi thanh toán thành công).
     */
    fun clearCart() {
        cartRef.removeValue()
    }

    /**
     * Reset trạng thái thanh toán về Idle sau khi thông báo được hiển thị.
     */
    fun resetPaymentState() {
        _paymentState.update { PaymentState.Idle }
    }

    override fun onCleared() {
        super.onCleared()
        // Ngừng lắng nghe khi ViewModel bị hủy
        cartRef.removeEventListener(valueEventListener)
    }
}