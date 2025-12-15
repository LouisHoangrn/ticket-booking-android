package com.finalexam.project.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // THÊM IMPORT NÀY
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope // THÊM IMPORT NÀY
import androidx.recyclerview.widget.LinearLayoutManager
import com.finalexam.project.adapter.CartAdapter
import com.finalexam.project.adapter.CartActionListener
import com.finalexam.project.databinding.FragmentCartBinding
import com.finalexam.project.model.CartItem
import com.finalexam.project.viewmodel.CartViewModel
import com.finalexam.project.viewmodel.PaymentState // THÊM IMPORT NÀY
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.launchIn // THÊM IMPORT NÀY
import kotlinx.coroutines.flow.onEach // THÊM IMPORT NÀY

import java.text.NumberFormat
import java.util.Locale

/**
 * Fragment cho màn hình Cart (Giỏ hàng).
 * Cấu hình hiển thị danh sách vé đã chọn dưới dạng danh sách dọc (1 cột).
 */
class CartFragment : Fragment(), CartActionListener {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    // KHỞI TẠO VIEWMODEL SỬ DỤNG FACTORY
    private val cartViewModel: CartViewModel by viewModels {
        // Lấy ID người dùng thực tế từ Firebase Auth.
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.e("CartFragment", "Lỗi: Không tìm thấy User ID! Giỏ hàng sẽ không hoạt động.")
            throw IllegalStateException("User must be logged in to view cart.")
        }

        val firebaseDb = FirebaseDatabase.getInstance()

        // Sử dụng userId thực tế để khởi tạo ViewModel
        CartViewModelFactory(firebaseDb, userId)
    }

    private lateinit var cartAdapter: CartAdapter
    // Khởi tạo NumberFormat để định dạng tiền tệ Việt Nam
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Khởi tạo Adapter và gán listener
        cartAdapter = CartAdapter(this)

        // 2. Thiết lập RecyclerView
        setupRecyclerView()

        // 3. Quan sát dữ liệu từ ViewModel
        observeCartData()

        // 4. Quan sát trạng thái Thanh toán (MỚI)
        observePaymentState()

        // 5. Thiết lập logic cho nút Checkout (Thanh Toán)
        binding.checkoutButton.setOnClickListener {
            if (cartViewModel.cartItems.value.isNullOrEmpty()) {
                // THÊM TOAST KHI GIỎ HÀNG TRỐNG
                Toast.makeText(context, "Giỏ hàng trống, không thể thanh toán.", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("CartFragment", "Bắt đầu quy trình thanh toán...")
                // GỌI HÀM THANH TOÁN MỚI
                cartViewModel.checkout()
            }
        }
    }

    /**
     * MỚI: Lắng nghe trạng thái thanh toán (Loading, Success, Error) và cập nhật UI.
     */
    private fun observePaymentState() {
        // Sử dụng Flow để lắng nghe trạng thái thanh toán.
        cartViewModel.paymentState
            .onEach { state ->
                when (state) {
                    is PaymentState.Idle -> {
                        // Tắt thanh tiến trình, kích hoạt lại nút
                        binding.progressBar.visibility = View.GONE
                        binding.checkoutButton.isEnabled = true
                    }
                    is PaymentState.Loading -> {
                        // Bật thanh tiến trình, vô hiệu hóa nút
                        binding.progressBar.visibility = View.VISIBLE
                        binding.checkoutButton.isEnabled = false
                    }
                    is PaymentState.Success -> {
                        // Tắt thanh tiến trình, hiển thị thông báo thành công
                        binding.progressBar.visibility = View.GONE
                        binding.checkoutButton.isEnabled = true
                        Toast.makeText(context, state.response.message, Toast.LENGTH_LONG).show()
                        // Rất quan trọng: Reset trạng thái sau khi thông báo
                        cartViewModel.resetPaymentState()
                    }
                    is PaymentState.Error -> {
                        // Tắt thanh tiến trình, hiển thị thông báo lỗi
                        binding.progressBar.visibility = View.GONE
                        binding.checkoutButton.isEnabled = true
                        Toast.makeText(context, "Lỗi: ${state.message}", Toast.LENGTH_LONG).show()
                        // Rất quan trọng: Reset trạng thái sau khi thông báo
                        cartViewModel.resetPaymentState()
                    }
                }
            }
            // Khởi chạy coroutine trong scope của Fragment
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    /**
     * Cấu hình RecyclerView với LinearLayoutManager (1 cột dọc).
     */
    private fun setupRecyclerView() {
        binding.recyclerViewCart.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    /**
     * Lắng nghe LiveData chứa danh sách CartItem và cập nhật UI.
     */
    private fun observeCartData() {
        // Lắng nghe danh sách item
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            // Cập nhật Adapter
            cartAdapter.submitList(items)

            // Cập nhật trạng thái giỏ hàng trống/có item
            updateCartVisibility(items.isEmpty())
        }

        // Lắng nghe tổng giá trị (tính toán bởi ViewModel)
        cartViewModel.totalPrice.observe(viewLifecycleOwner) { price ->
            val formattedPrice = formatCurrency(price)

            // Cập nhật tổng tiền ở khu vực tóm tắt
            binding.tvTotalPrice.text = formattedPrice

            // Cập nhật nút Checkout với tổng tiền
            binding.checkoutButton.text = "Thanh Toán $formattedPrice"
        }
    }

    /**
     * Cập nhật trạng thái hiển thị của RecyclerView, tổng cộng và thông báo giỏ hàng trống.
     */
    private fun updateCartVisibility(isEmpty: Boolean) {
        binding.recyclerViewCart.visibility = if (isEmpty) View.GONE else View.VISIBLE
        // Khu vực thanh toán/tổng cộng
        binding.clCheckout.visibility = if (isEmpty) View.GONE else View.VISIBLE
        // Thông báo giỏ hàng trống
        binding.emptyCartText.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    /**
     * Hàm tiện ích để định dạng giá trị double thành tiền tệ Việt Nam.
     */
    private fun formatCurrency(amount: Double): String {
        return currencyFormatter.format(amount)
    }

    /**
     * Xử lý khi người dùng nhấn nút xóa item (Implement CartActionListener).
     */
    override fun onRemoveItem(cartItemId: String) {
        // Gọi ViewModel để xóa item
        cartViewModel.removeItem(cartItemId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


/**
 * ViewModel Factory cần thiết để cung cấp dependency (FirebaseDatabase và userId)
 * cho CartViewModel khi nó được khởi tạo.
 */
class CartViewModelFactory(
    private val database: FirebaseDatabase,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(database, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}