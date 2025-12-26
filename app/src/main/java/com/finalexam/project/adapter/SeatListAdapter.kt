package com.finalexam.project.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.finalexam.project.R
import com.finalexam.project.databinding.ItemSeatBinding
import com.finalexam.project.model.Seat

/**
 * Adapter cho danh sách ghế ngồi, quản lý trạng thái chọn/bỏ chọn ghế.
 *
 * @param seatList Danh sách đối tượng Seat.
 * @param context Context của ứng dụng.
 * @param selectedSeat Listener để trả về thông tin ghế đã chọn (callback).
 */
class SeatListAdapter(
    private val seatList: List<Seat>,
    private val context: Context,
    private val selectedSeat: SelectedSeat
) : RecyclerView.Adapter<SeatListAdapter.ViewHolder>() {

    // Sử dụng Set để lưu trữ tên ghế đã chọn. Set đảm bảo không có trùng lặp.
    private val selectedSeatNames: MutableSet<String> = mutableSetOf()

    inner class ViewHolder(val binding: ItemSeatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSeatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val seat = seatList[position]
        holder.binding.seatTxt.text = seat.name

        // Áp dụng kiểu dáng dựa trên trạng thái hiện tại
        updateSeatStyle(holder.binding, seat.status)

        // Xử lý sự kiện click
        holder.binding.seatTxt.setOnClickListener {
            // Không làm gì nếu ghế không có sẵn
            if (seat.status == Seat.SeatStatus.UNAVAILABLE) {
                return@setOnClickListener
            }

            // Xử lý chuyển đổi trạng thái
            if (seat.status == Seat.SeatStatus.AVAILABLE) {
                // Chọn ghế: AVAILABLE -> SELECTED
                seat.status = Seat.SeatStatus.SELECTED
                selectedSeatNames.add(seat.name)
            } else if (seat.status == Seat.SeatStatus.SELECTED) {
                // Bỏ chọn ghế: SELECTED -> AVAILABLE
                seat.status = Seat.SeatStatus.AVAILABLE
                selectedSeatNames.remove(seat.name)
            }

            // Cập nhật lại giao diện của item đã thay đổi
            notifyItemChanged(position)

            // Trả kết quả về Activity/Fragment thông qua callback
            sendResult()
        }
    }

    /**
     * Cập nhật giao diện ghế dựa trên trạng thái
     */
    private fun updateSeatStyle(binding: ItemSeatBinding, status: Seat.SeatStatus) {
        when (status) {
            Seat.SeatStatus.AVAILABLE -> {
                binding.seatTxt.setBackgroundResource(R.drawable.ic_seat_available)
                binding.seatTxt.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            Seat.SeatStatus.SELECTED -> {
                binding.seatTxt.setBackgroundResource(R.drawable.ic_seat_selected)
                binding.seatTxt.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            Seat.SeatStatus.UNAVAILABLE -> {
                binding.seatTxt.setBackgroundResource(R.drawable.ic_seat_unavailable)
                binding.seatTxt.setTextColor(ContextCompat.getColor(context, R.color.grey))
            }
        }
    }

    /**
     * Gửi kết quả (tên ghế và số lượng) về listener
     */
    private fun sendResult() {
        val selectedNamesString = selectedSeatNames.joinToString(",")
        selectedSeat.Return(selectedNamesString, selectedSeatNames.size)
    }

    override fun getItemCount(): Int = seatList.size

    // -- - PHƯƠNG TH  ỨC MỚI KHẮC PHỤC LỖI UNRESOLVED REFERENCE ---
    /**
     * [FIX: Unresolved reference 'getSelectedSeats']
     * Trả về danh sách tên các ghế hiện đang được chọn.
     * Phương thức này cho phép Activity truy vấn đồng bộ danh sách ghế đã chọn bất cứ lúc nào.
     * @return List<String> Danh sách tên ghế đã chọn.
     */
    fun getSelectedSeats(): List<String> {
        return selectedSeatNames.toList()
    }
    // -------------------------------------------------------------

    /**
     * Interface cho callback khi danh sách ghế được chọn thay đổi
     */
    interface SelectedSeat {
        /**
         * @param selectedName Chuỗi các tên ghế được chọn (ví dụ: "A1,B2,C3")
         * @param num Số lượng ghế đã chọn
         */
        fun Return(selectedName: String, num: Int)
    }
}