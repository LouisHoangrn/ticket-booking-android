package com.finalexam.project.adapter

/**
 * Interface để tr uy ền dữ liệu ngày được chọn từ DateAdapter về Activity/Fragment.
 */
interface OnDateSelectedListener {
    /**
     * @param date Ngày được chọn (ví dụ: "Thứ Hai, 15/12")
     */
    fun onDateSelected(date: String)
}