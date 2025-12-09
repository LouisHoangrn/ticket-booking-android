package com.finalexam.project.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.finalexam.project.activity.DetailFilmActivity
import com.finalexam.project.adapter.BookmarkAdapter
import com.finalexam.project.databinding.FragmentBookmarkBinding
import com.finalexam.project.model.Film
import com.finalexam.project.viewmodel.FilmViewModel
import com.finalexam.project.viewmodel.FilmViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Fragment hiển thị danh sách các phim đã được người dùng lưu (Bookmark).
 * Fragment này quan sát LiveData để cập nhật UI tự động (real-time) khi dữ liệu thay đổi.
 */
class BookmarkFragment : Fragment() {

    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!
    private val TAG = "BookmarkFragment"

    // ViewModel được khởi tạo thông qua Factory
    private lateinit var viewModel: FilmViewModel
    private lateinit var bookmarkAdapter: BookmarkAdapter

    // Khởi tạo các thành phần Firebase cần thiết cho Factory
    private val database = FirebaseDatabase.getInstance()
    private val userId: String
        // Lấy ID người dùng hiện tại (hoặc ID mặc định nếu chưa đăng nhập)
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user_id"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)

        // 1. Khởi tạo ViewModel sử dụng Factory
        val factory = FilmViewModelFactory(database, userId)
        viewModel = ViewModelProvider(this, factory)[FilmViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        // 2. Bắt đầu quan sát LiveData Real-time
        observeViewModel()
    }

    /**
     * Thiết lập RecyclerView và Adapter.
     */
    private fun setupRecyclerView() {
        // Khởi tạo Adapter, truyền vào 2 listener
        bookmarkAdapter = BookmarkAdapter(
            context = requireContext(),
            onUnbookmarkClickListener = { film ->
                // SỬA LỖI ĐỒNG BỘ: Gọi hàm remove trực tiếp
                handleUnbookmark(film)
            },
            onItemClickListener = { film ->
                // Xử lý sự kiện Xem chi tiết
                navigateToDetail(film)
            }
        )

        binding.bookmarkRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = bookmarkAdapter
        }
    }

    /**
     * Quan sát LiveData từ ViewModel để cập nhật UI Realtime.
     */
    private fun observeViewModel() {
        // Hiển thị thanh tiến trình khi chờ dữ liệu lần đầu
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyListTxt.visibility = View.GONE

        viewModel.bookmarkedFilms.observe(viewLifecycleOwner) { films ->
            Log.d(TAG, "LiveData received ${films.size} bookmarked films.")
            binding.progressBar.visibility = View.GONE

            // Cập nhật Adapter sử dụng submitList từ ListAdapter/DiffUtil
            bookmarkAdapter.submitList(films)

            // Cập nhật trạng thái Rỗng
            if (films.isNullOrEmpty()) {
                binding.emptyListTxt.visibility = View.VISIBLE
            } else {
                binding.emptyListTxt.visibility = View.GONE
            }
        }
    }

    /**
     * Xử lý hành động Bỏ lưu (Unbookmark) thông qua ViewModel.
     */
    private fun handleUnbookmark(film: Film) {
        // Gọi ViewModel để thực hiện thao tác xóa trên Firebase.
        // Dữ liệu trong LiveData sẽ tự động được cập nhật.
        viewModel.removeBookmark(film)
        Toast.makeText(requireContext(), "Đã bỏ lưu: ${film.Title}", Toast.LENGTH_SHORT).show()
    }

    /**
     * Điều hướng đến màn hình chi tiết phim.
     */
    private fun navigateToDetail(film: Film) {
        val intent = Intent(requireContext(), DetailFilmActivity::class.java)
        intent.putExtra("object", film)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}