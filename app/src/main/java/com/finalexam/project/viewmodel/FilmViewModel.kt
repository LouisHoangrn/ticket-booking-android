package com.finalexam.project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finalexam.project.model.Film
import com.finalexam.project.repository.BookmarkRepository
import kotlinx.coroutines.launch

class FilmViewModel(private val repository: BookmarkRepository) : ViewModel() {

    // 1. Phim đã bookmark (Sử dụng trực tiếp LiveData từ Repository để cập nhật real-time)
    // Đây là Nguồn Sự Thật Duy Nhất (Single Source of Truth) cho danh sách bookmark.
    val bookmarkedFilms: LiveData<List<Film>> = repository.getBookmarkedFilms()

    /**
     * Thao tác XÓA bookmark (Sử dụng cho RecyclerView Adapter/ViewHolder).
     * @param film: Đối tượng Film cần xóa.
     */
    fun removeBookmark(film: Film) {
        viewModelScope.launch {
            try {
                repository.removeBookmark(film)
                // KHÔNG cần cập nhật LiveData vì Repository đã tự làm điều đó real-time.
            } catch (e: Exception) {
                // Xử lý lỗi (ví dụ: Log, Toast)
            }
        }
    }

    /**
     * Thao tác THÊM bookmark (Dùng cho DetailFilmActivity).
     * @param film: Đối tượng Film cần thêm.
     */
    fun addBookmark(film: Film) {
        viewModelScope.launch {
            try {
                repository.addBookmark(film)
            } catch (e: Exception) {
                // Xử lý lỗi (ví dụ: Log, Toast)
            }
        }
    }

    // Các hàm checkBookmarkStatus và LiveData liên quan đến trạng thái icon
    // đã bị loại bỏ vì logic đó đã được chuyển sang ValueEventListener real-time trong DetailFilmActivity.
}