package com.finalexam.project.model

import com.google.firebase.database.IgnoreExtraProperties // Bắt buộc cho Firebase
import java.io.Serializable
import com.finalexam.project.model.Cast // Đảm bảo import lớp Cast

@IgnoreExtraProperties // Thêm annotation này
data class Film(
    // Giữ nguyên các thuộc tính của bạn, tất cả đều là 'var'
    var Title: String?= null,
    var Description:String?=null,
    var Poster: String?=null,
    var time: String?=null,
    var Trailer:String?=null,
    var Imdb:Int=0,
    var Year:Int=0,
    var price: Double=0.0,
    var Genre: ArrayList<String> = ArrayList(),
    var Casts: ArrayList<Cast> = ArrayList()
) : Serializable
{
    // Constructor không tham số (BẮT BUỘC cho Firebase Realtime Database)
    constructor() : this(
        null, // Title
        null, // Description
        null, // Poster
        null, // time
        null, // Trailer
        0,    // Imdb
        0,    // Year
        0.0,  // price
        ArrayList(), // Genre
        ArrayList()  // Casts
    )
}