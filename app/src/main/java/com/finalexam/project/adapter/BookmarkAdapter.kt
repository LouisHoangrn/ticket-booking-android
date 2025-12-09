package com.finalexam.project.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.finalexam.project.R
import com.finalexam.project.databinding.ViewholderMovieBinding // Sử dụng layout của Movie Item
import com.finalexam.project.model.Film // Sử dụng Model Film
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter chuyên biệt cho BookmarkFragment.
 * Hiển thị danh sách các đối tượng Film đã được lưu (bookmarked).
 * Mục đích chính là xử lý logic BỎ LƯU (Unbookmark).
 */
class BookmarkAdapter(
    // Danh sách các phim đã lưu
    private val items: MutableList<Film>,
    private val context: Context,
    // Listener cho sự kiện khi người dùng nhấn vào nút Bookmark/Unbookmark (hành động BỎ LƯU)
    private val onUnbookmarkClickListener: (Film, Int) -> Unit,
    // Listener cho sự kiện khi người dùng nhấn vào toàn bộ item (đi đến DetailActivity)
    private val onItemClickListener: (Film) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {

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
        val film = items[position]

        // 1. Tải Poster Phim bằng Glide
        val requestOptions = RequestOptions().transform(CenterCrop())
        Glide.with(context)
            .load(film.Poster) // Truy cập thuộc tính Poster của Film
            .apply(requestOptions)
            .placeholder(R.drawable.ic_launcher_background) // Cần có placeholder
            .into(holder.binding.ivMoviePoster)

        // 2. Thiết lập dữ liệu (Sử dụng thuộc tính PascalCase)
        holder.binding.tvMovieTitle.text = film.Title
        holder.binding.tvMovieGenre.text = film.Genre.joinToString(", ")
        holder.binding.tvRating.text = film.Imdb.toString()
        holder.binding.tvPrice.text = formatter.format(film.price)

        // 3. Hiển thị icon Bookmark đã lưu (vì đây là danh sách phim đã lưu)
        // Icon này sẽ tượng trưng cho nút "Unbookmark"
        holder.binding.btnBookmark.setImageResource(R.drawable.bookmark)

        // 4. Xử lý sự kiện BỎ LƯU (Unbookmark)
        holder.binding.btnBookmark.setOnClickListener {
            onUnbookmarkClickListener(film, position)
        }

        // 5. Xử lý sự kiện bấm vào toàn bộ Item (đi đến DetailActivity)
        holder.binding.root.setOnClickListener {
            onItemClickListener(film)
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * Hàm tiện ích để cập nhật danh sách phim từ nguồn dữ liệu (Firestore)
     */
    fun updateData(newFilms: List<Film>) {
        items.clear()
        items.addAll(newFilms)
        notifyDataSetChanged()
    }
}