package com.finalexam.project.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.finalexam.project.databinding.FragmentBookmarkBinding

/**
 * Fragment cho màn hình Bookmark (Đã lưu).
 * Cấu hình hiển thị danh sách phim đã lưu dưới dạng lưới 2 cột.
 */
class BookmarkFragment : Fragment() {

    private var _binding: FragmentBookmarkBinding? = null
    // Sử dụng View Binding để truy cập các View
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Ánh xạ layout fragment_bookmark.xml
        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thiết lập RecyclerView để hiển thị 2 mục trên mỗi hàng
        setupRecyclerView()

        // TODO: Tải dữ liệu phim đã lưu từ Firestore và thiết lập Adapter tại đây
    }

    /**
     * Cấu hình RecyclerView với GridLayoutManager (2 cột).
     * Phù hợp để hiển thị các mục phim (movie viewholder) dưới dạng lưới.
     */
    private fun setupRecyclerView() {
        // *** DÙNG GridLayoutManager VỚI 2 CỘT ***
        binding.recyclerViewBookmark.layoutManager = GridLayoutManager(context, 2)
        // Hiện tại chưa có Adapter, nên danh sách sẽ chưa hiển thị gì.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}