package com.finalexam.project.adapter

/**
 * Interface để truyền dữ liệu giờ được chọn từ TimeAdapter về Activity/Fragment.
 */
interface OnTimeSelectedListener {
    /**
     * @param time Giờ chiếu được chọn (ví dụ: "19:30")
     */
    fun onTimeSelected(time: String)
}