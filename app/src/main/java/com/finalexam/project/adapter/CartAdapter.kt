package com.finalexam.project.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.finalexam.project.R
import com.finalexam.project.databinding.ViewholderCartItemBinding // View Binding cho layout Cart Item
import com.finalexam.project.model.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Interface để thông báo cho Fragment khi có thay đổi trong giỏ hàng (Total Price)
 * và xử lý các tương tác cần Database.
 */
interface CartUpdateListener {
    fun onCartChanged()
    // Thêm các phương thức để xử lý tương tác với Database
    fun onQuantityChanged(itemKey: String, newQuantity: Int)
    fun onRemoveItem(itemKey: String)
}

/**
 * Adapter cho RecyclerView hiển thị danh sách vé trong Giỏ hàng.
 * Xử lý các tương tác: Tăng/giảm số lượng, Xóa mục.
 * LƯU Ý: Adapter này yêu cầu model CartItem.kt phải có một thuộc tính
 * lưu trữ key (cartNodeKey) của node trên Firebase.
 */
class CartAdapter(
    private val items: MutableList<Pair<CartItem, String>>, // Sửa: Lưu cả CartItem và Key (String)
    private val listener: CartUpdateListener // Listener để cập nhật tổng tiền và tương tác DB
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private var context: Context? = null
    // Loại bỏ các tham chiếu Firebase không cần thiết khỏi Adapter
    // val auth = FirebaseAuth.getInstance()
    // val database = FirebaseDatabase.getInstance()
    // val userId = auth.currentUser?.uid
    // val cartRef = userId?.let { database.getReference("Users").child(it).child("Cart") }


    inner class ViewHolder(private val binding: ViewholderCartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pair: Pair<CartItem, String>) {
            val item = pair.first // CartItem object
            val itemKey = pair.second // Firebase Key

            // 1. Tên phim
            binding.tvItemName.text = item.filmTitle

            // 2. Giá tiền đơn vị
            // Hiển thị giá và định dạng VNĐ (Giả định tvItemPrice là TextView trong viewholder_cart_item.xml)
            binding.tvItemPrice.text = "${String.format("%,.0f", item.ticketPrice)} VNĐ"

            // 3. Chi tiết (Ngày, Giờ, Ghế)
            binding.tvItemDetails.text = "Ngày: ${item.showDate} | Giờ: ${item.showTime} | Ghế: ${item.selectedSeats}"

            // 4. Số lượng
            binding.tvQuantity.text = item.quantity.toString()

            // 5. Hình ảnh Poster
            context?.let { ctx ->
                Glide.with(ctx)
                    .load(item.filmPoster)
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(10)))
                    // Placeholder và Error (Giả định có sẵn R.drawable.movie_placeholder, ic_broken_image)
                    .placeholder(R.drawable.bg_green_rounded)
                    .error(R.drawable.light_black_bg)
                    .into(binding.ivItemImage)
            }

            // Xử lý tương tác:

            // Nút Giảm số lượng
            binding.btnQuantityMinus.setOnClickListener {
                if (item.quantity > 1) {
                    item.quantity--
                    // Cập nhật giao diện cục bộ
                    notifyItemChanged(adapterPosition)
                    // Yêu cầu Fragment cập nhật DB và tổng tiền
                    listener.onQuantityChanged(itemKey, item.quantity)
                }
            }

            // Nút Tăng số lượng
            binding.btnQuantityPlus.setOnClickListener {
                item.quantity++
                // Cập nhật giao diện cục bộ
                notifyItemChanged(adapterPosition)
                // Yêu cầu Fragment cập nhật DB và tổng tiền
                listener.onQuantityChanged(itemKey, item.quantity)
            }

            // Nút Xóa mục
            binding.btnRemoveItem.setOnClickListener {
                // Yêu cầu Fragment xóa mục này khỏi DB
                listener.onRemoveItem(itemKey)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderCartItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    /**
     * Hàm tiện ích để cập nhật dữ liệu.
     */
    fun updateData(newItems: List<Pair<CartItem, String>>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
        listener.onCartChanged() // Cập nhật tổng tiền sau khi thay đổi dữ liệu
    }
}