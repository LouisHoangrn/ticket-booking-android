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

class SeatListActivity : AppCompatActivity(),
    DateAdapter.OnDateSelectedListener,
    TimeAdapter.OnTimeSelectedListener,
    SeatListAdapter.SelectedSeat {

    private lateinit var binding: ActivitySeatListBinding
    private lateinit var seatListAdapter: SeatListAdapter
    private lateinit var cartViewModel: CartViewModel
    private val TAG = "SeatListActivity"

    // Giá trị cần lưu cho CartItem
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var selectedSeatNames: List<String> = emptyList()
    private var currentNumSelected: Int = 0

    // Giá cứng (Mock Data)
    private val FILM_TITLE = "Black Panther: Wakanda Forever"
    private val SINGLE_TICKET_PRICE = 75000.0 // 75,000 VNĐ
    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // Dependencies cho ViewModel
    private val database = FirebaseDatabase.getInstance()
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user_id"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        // 'this ' bây giờ là DateAdapter.OnDateSelectedListener (vì đã được khai báo ở trên)
        val dateAdapter = DateAdapter(dates, this)
        binding.dateRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dateAdapter
        }
    }

    private fun setupTimeRecyclerView() {
        // Mock Data for Times
        val times = listOf("10:00 AM", "02:00 PM", "06:30 PM", "09:45 PM")
        // 'this' bây giờ là TimeAdapter.OnTimeSelectedListener (vì đã được khai báo ở trên)
        val timeAdapter = TimeAdapter(times, this)
        binding.timeRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = timeAdapter
        }
    }

    private fun setupSeatRecyclerView() {
        val seatList = generateMockSeats()
        seatListAdapter = SeatListAdapter(seatList, this, this) // 'this' là SelectedSeat Listener
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
                val status = when {
                    (row == "B" && col > 5) || (row == "D" && col % 3 == 0) -> Seat.SeatStatus.UNAVAILABLE
                    else -> Seat.SeatStatus.AVAILABLE
                }
                // Sử dụng constructor Seat(status, name) mới
                seats.add(Seat(status, seatName))
            }
        }
        return seats
    }

    // --- IMPLEMENT LISTENER INTERFACES ---

    // onDateSelected là phương thức của DateAdapter.OnDateSelectedListener
    override fun onDateSelected(date: String) {
        selectedDate = date
        Log.d(TAG, "Ngày được chọn: $selectedDate")
        // Khi ngày thay đổi, có thể cần tải lại trạng thái ghế (ví dụ: gọi API)
    }

    // onTimeSelected là phương thức của TimeAdapter.OnTimeSelectedListener
    override fun onTimeSelected(time: String) {
        selectedTime = time
        Log.d(TAG, "Giờ được chọn: $selectedTime")
        // Khi giờ thay đổi, có thể cần tải lại trạng thái ghế (ví dụ: gọi API)
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

        // Tạo đối tượng Ca rtItem
        val cartItem = CartItem(
            filmId = 1, // Mock ID
            filmTitle = FILM_TITLE,
            filmPrice = SINGLE_TICKET_PRICE,
            selectedDate = selectedDate,
            selectedTime = selectedTime,
            seatNames = selectedSeatNames,
            quantity = currentNumSelected,
            totalPrice = totalAmount
        )

        // *** KHẮC PHỤC LỖI: Đổi tên hàm từ addToCart sang addItem  ***
        cartViewModel.addItem(cartItem)

        // Hiển thị thông báo và đóng Activity
        Toast.makeText(this, "Đã thêm $currentNumSelected vé vào Giỏ hàng!", Toast.LENGTH_LONG).show()
        finish()
    }
}