package com.finalexam.project.data

/**
 * Lớp Sealed để đóng gói trạng thái của các thao tác dữ liệu.
 * T: Kiểu dữ liệu trả về khi thành công.
 */
sealed class Result<out T> {

    /**
     * Trạng thái thành công, chứa dữ liệu trả về.
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Trạng thái lỗi, chứa thông tin exception.
     */
    data class Error(val exception: Exception) : Result<Nothing>()

    /**
     * Trạng thái đang tải dữ liệu.
     */
    object Loading : Result<Nothing>()
}