package com.finalexam.project.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Import Glide
import com.finalexam.project.R
import com.finalexam.project.databinding.ViewholderCartItemBinding
import com.finalexam.project.model.CartItem
import java.text.NumberFormat
import java.util.Locale

/**
 * Interface cho các hành động trên item giỏ hàng (hiện tại là Xóa).
 */
interface CartActionListener {
    fun onRemoveItem(cartItemId: String)
    // Có thể thêm onUpdateQuantity(cartItemId: String, newQuantity: Int) nếu cần
}

/**
 * Adapter sử dụng ListAdapter để tối ưu hiệu suất khi thay đổi dữ liệu (Realtime update).
 */
class CartAdapter(
    private val context: Context, // Thêm Context để dùng cho Glide
    private val listener: CartActionListener
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartItemDiffCallback()) {

    // Sử dụng định dạng tiền tệ Việt Nam (VNĐ)
    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    inner class CartViewHolder(private val binding: ViewholderCartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            // 1. Tải và hiển thị Poster Phim bằng Glide
            Glide.with(context) // Sử dụng Context được truyền vào Adapter
                .load(item.filmPosterUrl)
                .placeholder(R.drawable.bg_green_rounded) // Placeholder khi đang tải
                .error(R.drawable.bg_green_rounded) // Error fallback
                .centerCrop()
                .into(binding.ivItemImage)

            // 2. Tên phim
            binding.tvItemName.text = item.filmTitle

            // 3. TỔNG GIÁ cho gói vé này
            val totalCost = item.filmPrice * item.quantity
            binding.tvItemPrice.text = formatter.format(totalCost)

            // 4. Chi tiết: Ngày | Giờ | Ghế
            val seatString = item.seatNames.joinToString(", ")
            val singlePriceFormatted = formatter.format(item.filmPrice)
            binding.tvItemDetails.text = "Giá 1 vé: $singlePriceFormatted | Ngày: ${item.selectedDate} | Giờ: ${item.selectedTime} | Ghế: $seatString"

            // 5. Số lượng vé (tức là số lượng ghế)
            binding.tvQuantity.text = item.quantity.toString()

            // 6. Nút Xóa (btnRemoveItem)
            binding.btnRemoveItem.setOnClickListener {
                item.cartItemId?.let { id -> listener.onRemoveItem(id) }
            }

            // Do CartItem đại diện cho một gói vé đã chọn số lượng ghế,
            // chúng ta ẩn nút tăng/giảm số lượng để tránh thay đổi gói vé.
            binding.clQuantityControl.visibility = ViewGroup.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ViewholderCartItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffUtil callback để tối ưu hóa việc cập nhật RecyclerView.
     */
    private class CartItemDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.cartItemId == newItem.cartItemId
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}