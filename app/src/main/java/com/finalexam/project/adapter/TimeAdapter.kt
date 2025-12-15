package com.finalexam.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finalexam.project.R
import com.finalexam.project.databinding.ItemTimeBinding

/**
 * Adapter cho danh sách giờ chiếu   , sử dụng View Binding.
 */
class TimeAdapter(
    private val timeSlots: List<String>,
    // Listener hiện tại sử dụng interface lồng
    private val listener: OnTimeSelectedListener
) : RecyclerView.Adapter<TimeAdapter.ViewHolder>() {

    // Mặc định chọn giờ chiếu đầu tiên
    private var selectedPosition: Int = 0

    init {
        // Kích hoạt callback cho giờ chiếu mặc định ngay khi adapter được tạo
        if (timeSlots.isNotEmpty()) {
            listener.onTimeSelected(timeSlots[selectedPosition])
        }
    }

    inner class ViewHolder(private val binding: ItemTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(time: String) {

            binding.TextViewTime.text = time

            // Cập nhật giao diện dựa trên trạng thái chọn
            if (selectedPosition == bindingAdapterPosition) {
                // Trạng thái ĐƯỢC CHỌN (màu vàng)
                binding.TextViewTime.setBackgroundResource(R.drawable.yellow_bg) // Giả định yellow_bg là màu nền khi chọn
                binding.TextViewTime.setTextColor(binding.root.context.getColor(R.color.black))
            } else {
                // Trạng thái MẶC ĐỊNH (màu đen nhạt)
                binding.TextViewTime.setBackgroundResource(R.drawable.light_black_bg) // Giả định light_black_bg là màu nền mặc định
                binding.TextViewTime.setTextColor(binding.root.context.getColor(R.color.white))
            }

            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION && position != selectedPosition) {
                    val oldPosition = selectedPosition
                    selectedPosition = position

                    // Cập nhật UI cho vị trí cũ và vị trí mới
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)

                    // Thông báo cho Activity biết giờ chiếu nào được chọn
                    listener.onTimeSelected(timeSlots[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimeAdapter.ViewHolder {
        return ViewHolder(ItemTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: TimeAdapter.ViewHolder, position: Int) {
        holder.bind(timeSlots[position])
    }

    override fun getItemCount(): Int = timeSlots.size

    /**
     * Interface lồng (Nested Interface) cho callback khi một khung giờ (time slot) được chọn.
     * (Việc lồng giúp tránh lỗi Redeclaration khi có nhiều file adapter trong cùng package)
     */
    interface OnTimeSelectedListener {
        fun onTimeSelected(time: String)
    }
}