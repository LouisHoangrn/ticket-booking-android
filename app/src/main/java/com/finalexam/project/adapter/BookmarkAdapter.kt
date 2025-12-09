package com.finalexam.project.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.finalexam.project.R
import com.finalexam.project.databinding.ViewholderMovieBinding
import com.finalexam.project.model.Film
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter chuyên biệt cho BookmarkFragment, sử dụng ListAdapter và DiffUtil.
 * Mục đích chính là xử lý logic BỎ LƯU (Unbookmark) và hiển thị danh sách.
 * Sử dụng ListAdapter giúp cập nhật UI mượt mà hơn khi item bị xóa .
 */
class BookmarkAdapter(
    private val context: Context,
    // Listener cho sự kiện khi người dùng nhấn vào nút Unbookmark (chỉ cần đối tượng Film)
    private val onUnbookmarkClickListener: (Film) -> Unit,
    // Listener cho sự kiện khi người dùng nhấn vào toàn bộ item
    private val onItemClickListener: (Film) -> Unit
) : ListAdapter<Film, BookmarkAdapter.ViewHolder>(FilmDiffCallback()) {

    // Định dạng tiền tệ cho Việt Nam
    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // Lớp ViewHolder sử dụng ViewBinding cho viewholder_movie.xml
    inner class ViewHolder(val binding: ViewholderMovieBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Sử dụng getItem(position) của ListAdapter để lấy Film
        val film = getItem(position)

        // 1. Tải Poster Phim bằng Glide
        val requestOptions = RequestOptions().transform(CenterCrop())
        Glide.with(context)
            .load(film.Poster)
            .apply(requestOptions)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.binding.ivMoviePoster)

        // 2. Thiết lập dữ liệu (Sử dụng thuộc tính PascalCase)
        holder.binding.tvMovieTitle.text = film.Title
        holder.binding.tvMovieGenre.text = film.Genre.joinToString(", ")
        holder.binding.tvRating.text = film.Imdb.toString()
        holder.binding.tvPrice.text = formatter.format(film.price)

        // 3. Hiển thị icon Bookmark đã lưu (tượng trưng cho nút "Unbookmark")
        holder.binding.btnBookmark.setImageResource(R.drawable.bookmark)

        // 4. Xử lý sự kiện BỎ LƯU (Unbookmark)
        holder.binding.btnBookmark.setOnClickListener {
            // Gọi listener với đối tượng film hiện tại, Fragment sẽ xử lý logic ViewModel
            onUnbookmarkClickListener(film)
        }

        // 5. Xử lý sự kiện bấm vào toàn bộ Item
        holder.binding.root.setOnClickListener {
            onItemClickListener(film)
        }
    }

    /**
     * DiffUtil Callback để so sánh sự khác biệt giữa hai danh sách,
     * giúp RecyclerView hoạt động hiệu quả hơn.
     */
    class FilmDiffCallback : DiffUtil.ItemCallback<Film>() {
        override fun areItemsTheSame(oldItem: Film, newItem: Film): Boolean {
            // So sánh dựa trên ID duy nhất (Imdb)
            return oldItem.Imdb == newItem.Imdb
        }

        override fun areContentsTheSame(oldItem: Film, newItem: Film): Boolean {
            // So sánh toàn bộ nội dung của hai đối tượng Film
            return oldItem == newItem
        }
    }
}