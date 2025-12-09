package com.finalexam.project.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.finalexam.project.model.Film
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.tasks.await

/**
 * Repository xử lý các thao tác liên quan đến Bookmark (Thêm, Xóa, Kiểm tra trạng thái)
 * trên Firebase Realtime Database.
 */
class BookmarkRepository(
    private val database: FirebaseDatabase,
    private val userId: String
) {
    private val TAG = "BookmarkRepository"

    /**
     * Tham chiếu đến node chứa tất cả bookmarks của người dùng hiện tại:
     * Path: /Users/{userId}/Bookmarks
     */
    private val bookmarkRef = database.getReference("Users").child(userId).child("Bookmarks")

    // --- CÁC HÀM ĐÃ SỬA Ở LẦN TRƯỚC (Thêm/Xóa/Kiểm tra) ---
    suspend fun addBookmark(film: Film) {
        val filmId = getFilmImdbId(film)
        if (filmId != null) {
            try {
                bookmarkRef.child(filmId.toString()).setValue(film).await()
                Log.d(TAG, "Đã thêm phim ID $filmId vào bookmark thành công.")
            } catch (e: Exception) {
                Log.e(TAG, "LỖI: Không thể thêm bookmark cho phim ID $filmId. Message: ${e.message}", e)
                throw e
            }
        } else {
            throw IllegalArgumentException("Film ID (Imdb) không hợp lệ hoặc bị thiếu.")
        }
    }

    suspend fun removeBookmark(film: Film) {
        val filmId = getFilmImdbId(film)
        if (filmId != null) {
            try {
                bookmarkRef.child(filmId.toString()).removeValue().await()
                Log.d(TAG, "Đã xóa phim ID $filmId khỏi bookmark thành công.")
            } catch (e: Exception) {
                Log.e(TAG, "LỖI: Không thể xóa bookmark cho phim ID $filmId. Message: ${e.message}", e)
                throw e
            }
        } else {
            throw IllegalArgumentException("Film ID (Imdb) không hợp lệ hoặc bị thiếu.")
        }
    }

    suspend fun isFilmBookmarked(filmImdbId: Int): Boolean {
        return try {
            val snapshot = bookmarkRef.child(filmImdbId.toString()).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Log.e(TAG, "LỖI: Không thể kiểm tra trạng thái bookmark cho ID $filmImdbId. Message: ${e.message}", e)
            throw e
        }
    }

    // --- HÀM MỚI: Lấy danh sách phim đã lưu (Sử dụng LiveData cho real-time) ---
    /**
     * Lấy danh sách các bộ phim đã được bookmark theo thời gian thực (Real-time).
     * @return LiveData<List<Film>> danh sách phim.
     */
    fun getBookmarkedFilms(): LiveData<List<Film>> {
        val liveData = MutableLiveData<List<Film>>()

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookmarkedFilms = mutableListOf<Film>()

                // Lặp qua tất cả các con (mỗi con là 1 bộ phim)
                for (childSnapshot in snapshot.children) {
                    // Cố gắng chuyển đổi DataSnapshot thành đối tượng Film
                    val film = childSnapshot.getValue<Film>()
                    if (film != null) {
                        bookmarkedFilms.add(film)
                    } else {
                        Log.w(TAG, "Lỗi chuyển đổi dữ liệu Film từ Firebase: ${childSnapshot.key}")
                    }
                }

                liveData.value = bookmarkedFilms
                Log.d(TAG, "Đã tải ${bookmarkedFilms.size} phim bookmark.")
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi đọc dữ liệu từ Firebase (Ví dụ: Lỗi quyền truy cập)
                Log.e(TAG, "Lỗi đọc dữ liệu Bookmarks từ Firebase: ${error.message}")
                liveData.value = emptyList()
            }
        }

        // Đính kèm listener để lắng nghe thay đổi
        bookmarkRef.addValueEventListener(listener)

        // Trả về LiveData và cần nhớ xóa listener khi không dùng nữa (thường là trong ViewModel onCleared)
        return liveData
    }


    private fun getFilmImdbId(film: Film): Int? {
        return when (val imdbValue = film.Imdb) {
            is Double -> imdbValue.toInt()
            is Int -> imdbValue
            is Number -> imdbValue.toInt()
            is String -> imdbValue.toIntOrNull()
            else -> null
        }
    }
}