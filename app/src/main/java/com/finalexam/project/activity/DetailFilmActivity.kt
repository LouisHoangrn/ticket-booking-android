package com.finalexam.project.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.finalexam.project.R
import com.finalexam.project.adapter.CastListAdapter
import com.finalexam.project.adapter.GenreEachFilmAdapter
import com.finalexam.project.databinding.ActivityDetailFilmBinding
import com.finalexam.project.model.Film
import com.finalexam.project.repository.BookmarkRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.launch

/**
 * Activity hiển thị chi tiết một bộ phim và quản lý trạng thái lưu (Bookmark) của người dùng.
 */
class DetailFilmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailFilmBinding

    // Khởi tạo Repository
    private lateinit var bookmarkRepository: BookmarkRepository

    private var isFilmBookmarked = false
    private lateinit var currentFilm: Film

    // Dùng Imdb (Int) làm ID duy nhất cho Firebase và Repository. Khởi tạo giá trị mặc định an toàn.
    private var filmImdbId: Int = -1
    private val TAG = "DetailFilmActivity"

    // GIÁ VÉ MẶC ĐỊNH (Đồng bộ với giá đã hardcode trong SeatListActivity)
    private val SINGLE_TICKET_PRICE = 75000.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailFilmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Khởi tạo Firebase Dependencies
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user_id"

        // 2. KHỞI TẠO BookmarkRepository VỚI CÁC THAM SỐ
        bookmarkRepository = BookmarkRepository(database, userId)

        setVariable()
    }

    private fun setVariable() {
        // Lấy Film object từ Intent
        val film = intent.getSerializableExtra("object") as? Film ?: run {
            Toast.makeText(this, "Không tìm thấy dữ liệu phim.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        currentFilm = film

        // SỬA LỖI 1: Xử lý an toàn thuộc tính Imdb
        filmImdbId = when (film.Imdb) {
            is Double -> film.Imdb.toInt()
            is Int -> film.Imdb
            else -> {
                Log.e(TAG, "Imdb value is neither Int nor Double: ${film.Imdb}")
                -1
            }
        }

        if (filmImdbId == -1) {
            Toast.makeText(this, "Không tìm thấy ID phim hợp lệ (IMDB).", Toast.LENGTH_SHORT).show()
            return
        }

        // --- Thiết lập UI (Giữ nguyên) ---
        Glide.with(this)
            .load(film.Poster)
            .apply(RequestOptions().transform(CenterCrop()))
            .into(binding.filmPic)

        binding.titleTxt.text = film.Title
        binding.imdbTxt.text = "IMDB ${film.Imdb}"
        binding.movieTimeTxt.text = "${film.Year} - ${film.time}"
        binding.movieSummeryTxt.text = film.Description

        binding.backBtn.setOnClickListener {
            finish()
        }

        // Setup BlurView
        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = decorView.background

        binding.blurView.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(this))
            .setBlurRadius(18f)
            .setHasFixedTransformationMatrix(true)

        binding.blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.blurView.clipToOutline = true

        film.Genre?.let {
            binding.genreView.adapter = GenreEachFilmAdapter(it)
            binding.genreView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }

        film.Casts?.let {
            binding.castListView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.castListView.adapter = CastListAdapter(it)
        }


        // *** SỬA LỖI TRUYỀN DỮ LIỆU PHIM CHO SEATLISTACTIVITY ***
        binding.buyTicketBtn.setOnClickListener {
            val intent = Intent(this, SeatListActivity::class.java).apply {
                // Truyền ID phim (dùng IMDB ID)
                putExtra(SeatListActivity.EXTRA_FILM_ID, filmImdbId)
                // Truyền Tên phim
                putExtra(SeatListActivity.EXTRA_FILM_TITLE, currentFilm.Title)
                // Truyền Giá vé (sử dụng giá hardcode, cần được điều chỉnh nếu có giá động)
                putExtra(SeatListActivity.EXTRA_FILM_PRICE, SINGLE_TICKET_PRICE)
                // *** THÊM MỚI: Truyền URL Poster ***
                putExtra(SeatListActivity.EXTRA_FILM_POSTER, currentFilm.Poster)
            }
            startActivity(intent)
        }

        // --- Logic Bookmark ---

        // 1. Kiểm tra trạng thái Bookmark khi Activity load
        checkBookmarkStatus()

        // 2. Thiết lập sự kiện click cho nút Bookmark
        binding.bookmarkBtn.setOnClickListener {
            toggleBookmark()
        }
    }

    /**
     * Kiểm tra trạng thái Bookmark hiện tại của phim và cập nhật UI.
     */
    private fun checkBookmarkStatus() {
        if (filmImdbId == -1) return

        lifecycleScope.launch {
            try {
                // Gọi đúng hàm `isFilmBookmarked` và truyền đúng kiểu `Int`
                isFilmBookmarked = bookmarkRepository.isFilmBookmarked(filmImdbId)
                updateBookmarkButton(isFilmBookmarked)
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi kiểm tra trạng thái bookmark: ${e.message}")
                Toast.makeText(this@DetailFilmActivity, "Lỗi kết nối Firebase.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Đảo ngược trạng thái Bookmark: Thêm hoặc Xóa.
     */
    private fun toggleBookmark() {
        if (filmImdbId == -1) {
            Toast.makeText(this, "Không có ID phim để lưu.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                if (isFilmBookmarked) {
                    // Gọi đúng hàm `removeBookmark` và truyền đúng đối tượng `Film`
                    bookmarkRepository.removeBookmark(currentFilm)
                    isFilmBookmarked = false
                    Toast.makeText(this@DetailFilmActivity, "Đã xóa khỏi danh sách đã lưu", Toast.LENGTH_SHORT).show()
                } else {
                    // Gọi đúng hàm `addBookmark` và truyền đúng đối tượng `Film`
                    bookmarkRepository.addBookmark(currentFilm)
                    isFilmBookmarked = true
                    Toast.makeText(this@DetailFilmActivity, "Đã thêm vào danh sách đã lưu", Toast.LENGTH_SHORT).show()
                }
                updateBookmarkButton(isFilmBookmarked)
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi thay đổi trạng thái bookmark: ${e.message}")
                Toast.makeText(this@DetailFilmActivity, "Lỗi: Không thể thay đổi trạng thái lưu.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Cập nhật icon và màu sắc của nút Bookmark2.
     */
    private fun updateBookmarkButton(bookmarked: Boolean) {
        if (bookmarked) {
            binding.bookmarkBtn.setImageResource(R.drawable.outline_bookmark_added_24)
            binding.bookmarkBtn.setColorFilter(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.bookmarkBtn.setImageResource(R.drawable.bookmark)
            binding.bookmarkBtn.setColorFilter(ContextCompat.getColor(this, R.color.white))
        }
    }
}