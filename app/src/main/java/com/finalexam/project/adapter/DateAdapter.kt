package com.finalexam.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finalexam.project.R
import com.finalexam.project.databinding.ItemDateBinding

/**
 * Adapter cho danh sách ngày (Dat e), sử dụng View Binding.
 */
class DateAdapter(
    private val timeSlots: List<String>,
    private val listener: OnDateSelectedListener // Sử dụng Nested In terface
) : RecyclerView.Adapter<DateAdapter.ViewHolder>() {

    private var selectedPosition = 0 // Mặc định chọn ngày đầu tiên (position 0)

    init {
        // Kích hoạt callback cho ngày mặc định ngay khi adapter được tạo
        if (timeSlots.isNotEmpty()) {
            listener.onDateSelected(timeSlots[selectedPosition])
        }
    }

    inner class ViewHolder(private val binding: ItemDateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            val dateParts = date.split("/")
            if (dateParts.size == 3) {
                // Hiển thị định dạng Mon/15/Apr
                binding.dayTxt.text = dateParts[0] // Mon, Tue, ...
                binding.dayMonthTxt.text = dateParts[1] + " " + dateParts[2] // 15 Apr, 16 Apr, ...

                // Cập nhật giao diện dựa trên trạng thái chọn
                if (selectedPosition == bindingAdapterPosition) {
                    // Trạng thái ĐƯỢC CHỌN
                    binding.mainLayout.setBackgroundResource(R.drawable.white_bg) // Giả định white_bg là màu nền khi chọn
                    binding.dayTxt.setTextColor(binding.root.context.getColor(R.color.black))
                    binding.dayMonthTxt.setTextColor(binding.root.context.getColor(R.color.black))
                } else {
                    // Trạng thái MẶC ĐỊNH
                    binding.mainLayout.setBackgroundResource(R.drawable.light_black_bg) // Giả định light_black_bg là màu nền mặc định
                    binding.dayTxt.setTextColor(binding.root.context.getColor(R.color.white))
                    binding.dayMonthTxt.setTextColor(binding.root.context.getColor(R.color.white))
                }

                // Xử lý sự kiện click
                binding.root.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION && position != selectedPosition) {
                        val oldPosition = selectedPosition
                        selectedPosition = position

                        // Cập nhật UI cho vị trí cũ và vị trí mới
                        notifyItemChanged(oldPosition)
                        notifyItemChanged(selectedPosition)

                        // Thông báo cho Activity biết ngày nào được chọn
                        listener.onDateSelected(timeSlots[position])
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DateAdapter.ViewHolder {
        return ViewHolder(ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: DateAdapter.ViewHolder, position: Int) {
        holder.bind(timeSlots[position])
    }

    override fun getItemCount(): Int = timeSlots.size

    /**
     * Interface lồng (Nested Interface) cho callback khi một ngày được chọn.
     * (Việc lồng giúp tránh lỗi Redeclaration)
     */
    interface OnDateSelectedListener {
        fun onDateSelected(date: String)
    }
}