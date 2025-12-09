package com.finalexam.project.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.finalexam.project.databinding.FragmentCartBinding

/**
 * Fragment cho màn hình Cart (Giỏ hàng).
 * Cấu hình hiển thị danh sách vé đã chọn dưới dạng danh sách dọc (1 cột).
 */
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    // Sử dụng View Binding để truy cập các View
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Ánh xạ layout fragment_cart.xml
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thiết lập RecyclerView
        setupRecyclerView()

        // TODO: Tải dữ liệu giỏ hàng từ Firestore và thiết lập Adapter tại đây
        // TODO: Thiết lập logic cho nút Checkout (Thanh Toán)
    }

    /**
     * Cấu hình RecyclerView với LinearLayoutManager (1 cột dọc).
     * Phù hợp cho danh sách các mục giỏ hàng.
     */
    private fun setupRecyclerView() {
        // Giỏ hàng thường là danh sách 1 cột nên dùng LinearLayoutManager
        binding.recyclerViewCart.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}