package com.finalexam.project.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.finalexam.project.R
import com.finalexam.project.adapter.DateAdapter
import com.finalexam.project.adapter.SeatListAdapter
import com.finalexam.project.adapter.TimeAdapter
import com.finalexam.project.databinding.ActivitySeatListBinding
import com.finalexam.project.model.CartItem
import com.finalexam.project.model.Seat
import com.finalexam.project.viewmodel.CartViewModel
import com.finalexam.project.viewmodel.CartViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Locale

/**
 * Activity cho phép người dùng chọn ngày, giờ và ghế ngồi cho một bộ phim.
 * Sau đó, tạo và thêm CartItem vào giỏ hàng Firebase.
 */
class SeatListActivity : AppCompatActivity(),
    DateAdapter.OnDateSelectedListener,
    TimeAdapter.OnTimeSelectedListener,
    SeatListAdapter.SelectedSeat {

    // -------------------------------------------------------------------------
    // CÁC HẰNG SỐ ĐỂ NHẬN DỮ LIỆU TỪ INTENT
    companion object {
        const val EXTRA_FILM_ID = "extra_film_id"
        const val EXTRA_FILM_TITLE = "extra_film_title"
        const val EXTRA_FILM_PRICE = "extra_film_price"
        const val EXTRA_FILM_POSTER = "extra_film_poster" // ĐÃ THÊM: Hằng số cho Poster URL
    }
    // -------------------------------------------------------------------------

    private lateinit var binding: ActivitySeatListBinding
    private lateinit var seatListAdapter: SeatListAdapter
    private lateinit var cartViewModel: CartViewModel
    private val TAG = "SeatListActivity"

    // Giá trị cần lưu cho CartItem
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var selectedSeatNames: List<String> = emptyList()
    private var currentNumSelected: Int = 0

    // Giá trị Phim (Được khởi tạo từ Intent hoặc dùng giá trị mặc định)
    private var FILM_ID: Int = 0
    private var FILM_TITLE: String = ""
    private var SINGLE_TICKET_PRICE: Double = 0.0 // Giá 1 vé
    private var FILM_POSTER_URL: String = "" // ĐÃ THÊM: Thuộc tính lưu Poster URL

    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // Dependencies cho ViewModel
    private val database = FirebaseDatabase.getInstance()
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user_id"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ---------------------------------------------------------------------
        // 1. LẤY DỮ LIỆU PHIM TỪ INTENT
        val intentExtras = intent.extras
        if (intentExtras != null) {
            // Lấy dữ liệu thực tế được truyền từ màn hình chi tiết phim
            FILM_ID = intentExtras.getInt(EXTRA_FILM_ID, 1) // Default 1
            FILM_TITLE = intentExtras.getString(EXTRA_FILM_TITLE, "Black Panther: Wakanda Forever")!!
            SINGLE_TICKET_PRICE = intentExtras.getDouble(EXTRA_FILM_PRICE, 75000.0) // Default 75k
            FILM_POSTER_URL = intentExtras.getString(EXTRA_FILM_POSTER, "") ?: "" // ĐÃ THÊM: Lấy Poster URL
        } else {
            // Trường hợp không có Intent Extras (ví dụ: chạy test)
            FILM_ID = 1
            FILM_TITLE = "Black Panther: Wakanda Forever (Mặc định)"
            SINGLE_TICKET_PRICE = 75000.0
            FILM_POSTER_URL = "" // Mặc định là chuỗi rỗng
        }
        // Hiển thị tên phim (Nếu Activity có TextView hiển thị tên phim)
        // binding.filmTitleTxt.text = FILM_TITLE
        Log.d(TAG, "Đang đặt vé cho: $FILM_TITLE (ID: $FILM_ID, Poster: $FILM_POSTER_URL, Giá: $SINGLE_TICKET_PRICE)")
        // ---------------------------------------------------------------------

        // Khởi tạo ViewModel
        val factory = CartViewModelFactory(database, userId)
        cartViewModel = ViewModelProvider(this, factory)[CartViewModel::class.java]

        setupDateRecyclerView()
        setupTimeRecyclerView()
        setupSeatRecyclerView()

        // Đổi tên nút và thêm logic Thêm vào Giỏ hàng
        binding.button.setOnClickListener {
            addToCart()
        }

        // Cập nhật giá trị mặc định ban đầu
        updatePriceDisplay()
    }

    // --- SETUP RECYCLERVIEWS ---

    private fun setupDateRecyclerView() {
        // Mock Data for Dates
        val dates = listOf("Mon/15/Apr", "Tue/16/Apr", "Wed/17/Apr", "Thu/18/Apr", "Fri/19/Apr")
        val dateAdapter = DateAdapter(dates, this)
        binding.dateRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dateAdapter
        }
        // Chọn ngày đầu tiên làm mặc định nếu danh sách không rỗng
        if (dates.isNotEmpty()) {
            selectedDate = dates[0]
        }
    }

    private fun setupTimeRecyclerView() {
        // Mock Data for Times
        val times = listOf("10:00 AM", "02:00 PM", "06:30 PM", "09:45 PM")
        val timeAdapter = TimeAdapter(times, this)
        binding.timeRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = timeAdapter
        }
        // Chọn giờ đầu tiên làm mặc định nếu danh sách không rỗng
        if (times.isNotEmpty()) {
            selectedTime = times[0]
        }
    }

    private fun setupSeatRecyclerView() {
        val seatList = generateMockSeats()
        // Đã sửa 'this' thứ 3 thành 'this' (SelectedSeat Listener)
        seatListAdapter = SeatListAdapter(seatList, this, this)
        binding.seatRecyclerview.apply {
            layoutManager = GridLayoutManager(this@SeatListActivity, 8) // 8 cột ghế
            adapter = seatListAdapter
        }
    }

    /**
     * Tạo danh sách ghế giả lập (A1 -> D8)
     */
    private fun generateMockSeats(): List<Seat> {
        val seats = mutableListOf<Seat>()
        val rows = listOf("A", "B", "C", "D")
        for (row in rows) {
            for (col in 1..8) {
                val seatName = "$row$col"
                // Giả định SeatStatus.UNAVAILABLE tồn tại trong model.Seat
                val status = when {
                    (row == "B" && col > 5) || (row == "D" && col % 3 == 0) -> Seat.SeatStatus.UNAVAILABLE
                    else -> Seat.SeatStatus.AVAILABLE
                }
                // Giả định constructor của Seat là Seat(status: SeatStatus, name: String)
                seats.add(Seat(status, seatName))
            }
        }
        return seats
    }

    // --- IMPLEMENT LISTENER INTERFACES ---

    override fun onDateSelected(date: String) {
        selectedDate = date
        Log.d(TAG, "Ngày được chọn: $selectedDate")
        // Tải lại trạng thái ghế nếu cần
    }

    override fun onTimeSelected(time: String) {
        selectedTime = time
        Log.d(TAG, "Giờ được chọn: $selectedTime")
        // Tải lại trạng thái ghế nếu cần
    }

    // Listener từ SeatListAdapter
    override fun Return(selectedName: String, num: Int) {
        // Cập nhật thông tin ghế đã chọn và số lượng
        currentNumSelected = num
        selectedSeatNames = seatListAdapter.getSelectedSeats()
        Log.d(TAG, "Ghế đã chọn ($num): $selectedSeatNames")
        updatePriceDisplay()
    }

    // --- LOGIC GIỎ HÀNG VÀ UI ---

    private fun updatePriceDisplay() {
        val totalAmount = currentNumSelected * SINGLE_TICKET_PRICE
        binding.priceTxt.text = formatter.format(totalAmount)
        binding.numberSelectedTxt.text = "$currentNumSelected Ghế đã chọn"
    }

    private fun addToCart() {
        if (currentNumSelected == 0) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một ghế.", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Lỗi: Ngày hoặc Giờ chưa được chọn.", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmount = currentNumSelected * SINGLE_TICKET_PRICE

        // Tạo đối tượng CartItem với dữ liệu phim ĐÃ NHẬN TỪ INTENT
        val cartItem = CartItem(
            filmId = FILM_ID, // Dữ liệu từ Intent
            filmTitle = FILM_TITLE, // Dữ liệu từ Intent
            filmPrice = SINGLE_TICKET_PRICE, // Dữ liệu từ Intent
            filmPosterUrl = FILM_POSTER_URL, // ĐÃ THÊM: Dữ liệu Poster URL
            selectedDate = selectedDate,
            selectedTime = selectedTime,
            seatNames = selectedSeatNames,
            quantity = currentNumSelected,
            totalPrice = totalAmount
        )

        // Thêm vào giỏ hàng qua ViewModel
        cartViewModel.addItem(cartItem)

        // Hiển thị thông báo và đóng Activity
        Toast.makeText(this, "Đã thêm $currentNumSelected vé vào Giỏ hàng!", Toast.LENGTH_LONG).show()
        finish()
    }
}