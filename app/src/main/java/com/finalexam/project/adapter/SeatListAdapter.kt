package com.finalexam.project.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.finalexam.project.R
import com.finalexam.project.databinding.ItemSeatBinding
import com.finalexam.project.model.Seat

class SeatListAdapter(
    private val seatList: List<Seat>,
    private val context: Context,
    private val selectedSeat: SelectedSeat
) : RecyclerView.Adapter<SeatListAdapter.ViewHolder>() {

    // Sửa lỗi chính tả + khởi tạo đúng
    private val selectedSeatName = ArrayList<String>()

    inner class ViewHolder(val binding: ItemSeatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSeatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val seat = seatList[position]
        holder.binding.seatTxt.text = seat.name

        // Cập nhật giao diện theo trạng thái ghế
        when (seat.status) {
            Seat.SeatStatus.AVAILABLE -> {
                holder.binding.seatTxt.setBackgroundResource(R.drawable.ic_seat_available)
                holder.binding.seatTxt.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            Seat.SeatStatus.SELECTED -> {
                holder.binding.seatTxt.setBackgroundResource(R.drawable.ic_seat_selected)
                holder.binding.seatTxt.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            Seat.SeatStatus.UNAVAILABLE -> {
                holder.binding.seatTxt.setBackgroundResource(R.drawable.ic_seat_unavailable)
                holder.binding.seatTxt.setTextColor(ContextCompat.getColor(context, R.color.grey))
            }
        }

        // Xử lý click chọn/ghế
        holder.binding.seatTxt.setOnClickListener {
            if (seat.status == Seat.SeatStatus.UNAVAILABLE) return@setOnClickListener

            if (seat.status == Seat.SeatStatus.AVAILABLE) {
                // Chọn ghế
                seat.status = Seat.SeatStatus.SELECTED
                selectedSeatName.add(seat.name)
            } else if (seat.status == Seat.SeatStatus.SELECTED) {
                // Bỏ chọn ghế → PHẢI REMOVE, không phải add!
                seat.status = Seat.SeatStatus.AVAILABLE
                selectedSeatName.remove(seat.name)
            }

            // Cập nhật lại item hiện tại
            notifyItemChanged(position)

            // Trả kết quả về Activity/Fragment
            val selectedNames = selectedSeatName.joinToString(",")
            selectedSeat.Return(selectedNames, selectedSeatName.size)
        }
    }

    override fun getItemCount(): Int = seatList.size

    interface SelectedSeat {
        fun Return(selectedName: String, num: Int)
    }
}