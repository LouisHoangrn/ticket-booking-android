package com.finalexam.project.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.finalexam.project.adapter.FilmListAdapter
import com.finalexam.project.adapter.SliderAdapter
import com.finalexam.project.databinding.ActivityMainBinding
import com.finalexam.project.model.Film
import com.finalexam.project.model.SliderItems
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.abs
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    // Thay đổi thành nullable để xử lý lỗi khởi tạo Firebase an toàn hơn
    private var database: FirebaseDatabase? = null
    // Dùng Looper chính để đảm bảo Handler hoạt động đúng
    private val sliderHandle = Handler(Looper.getMainLooper())

    private val sliderRunnable = Runnable {
        // Logic kiểm tra giới hạn để tránh IndexOutOfBoundsException và reset vòng lặp
        if (binding.viewPager2.adapter != null && binding.viewPager2.currentItem < binding.viewPager2.adapter!!.itemCount - 1) {
            binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1
        } else {
            binding.viewPager2.currentItem = 0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // BƯỚC 1: Xử lý View Binding/Layout an toàn
        runCatching {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
        }.onFailure { e ->
            Log.e("MainActivity", "CRITICAL: Lỗi khởi tạo Layout. Lỗi: ${e.message}", e)
            throw e
        }

        // BƯỚC 2: Khởi tạo Firebase an toàn.
        database = runCatching {
            FirebaseDatabase.getInstance()
        }.onFailure { e ->
            Log.e("MainActivity", "CRITICAL: Firebase Realtime Database KHÔNG THỂ khởi tạo. Lỗi: ${e.message}", e)
        }.getOrNull()

        // BƯỚC 3: Cập nhật Profile và Tải dữ liệu
        updateProfileHeader()

        // Chỉ tải dữ liệu phim nếu Database được khởi tạo thành công
        if (database != null) {
            initBanner()
            initTopMovies()
            initUpcomming()
        } else {
            Log.w("MainActivity", "Firebase Database chưa khởi tạo. Không tải dữ liệu phim.")
        }

        // Log out
        // Bảo vệ View Binding cho btnLogout
        runCatching {
            binding.btnLogout.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }.onFailure { e ->
            Log.e("MainActivity", "LỖI CRITICAL: Không tìm thấy ID 'btnLogout' trong Layout. Lỗi: ${e.message}", e)
        }
    }

    /**
     * Tối ưu hóa logic cập nhật Profile:
     * Sử dụng chuỗi toán tử Kotlin (?. , ?:, takeIf) để rút gọn và làm sạch code.
     */
    private fun updateProfileHeader() {
        // Vẫn giữ runCatching để bảo vệ View Binding cho textView3 và textView4
        runCatching {
            val user = FirebaseAuth.getInstance().currentUser

            // 1. Xử lý Email (textView4)
            val email = user?.email ?: "guest@movieapp.com"
            binding.textView4.text = email

            // 2. Xử lý Tên người dùng (textView3)
            val name = user?.email
                ?.substringBefore("@") // Lấy phần trước ký tự @
                ?.takeIf { it.isNotBlank() } // Chỉ tiếp tục nếu chuỗi không rỗng
                ?.replaceFirstChar { it.titlecase() } // Viết hoa chữ cái đầu
                ?: "Khách Hàng" // Giá trị mặc định nếu user là null hoặc email rỗng

            binding.textView3.text = name

        }.onFailure { e ->
            Log.e("MainActivity", "LỖI CRITICAL: Không tìm thấy ID 'textView3' hoặc 'textView4' cho Profile Header. Lỗi: ${e.message}", e)
        }
    }

    private fun initTopMovies() {
        val db = database ?: return // Thoát nếu database null
        val myRef: DatabaseReference = db.getReference("Items")
        binding.progressBarTopMovies.visibility= View.VISIBLE
        val items = ArrayList<Film>()

        myRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(i in snapshot.children){
                        val item = i.getValue(Film::class.java)
                        if(item!=null){
                            items.add(item)
                        }
                    }

                    if(items.isNotEmpty()){
                        binding.recyclerViewTopMovies.layoutManager=
                            LinearLayoutManager(
                                this@MainActivity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        binding.recyclerViewTopMovies.adapter = FilmListAdapter(items)
                    }
                    binding.progressBarTopMovies.visibility= View.GONE

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Sửa lỗi TODO: Ghi log thay vì để trống
                Log.e("Firebase", "Lỗi tải Top Movies: ${error.message}")
            }

        })
    }

    private fun initBanner(){
        val db = database ?: return // Thoát nếu database null
        val myRef = db.getReference("Banners")
        binding.progressBarSlider.visibility = View.VISIBLE

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<SliderItems>()
                for (i in snapshot.children){
                    val list = i.getValue(SliderItems::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                binding.progressBarSlider.visibility = View.GONE
                banners(lists)
            }

            override fun onCancelled(error: DatabaseError) {
                // Sửa lỗi TODO: Ghi log thay vì để trống
                Log.e("Firebase", "Lỗi tải Banners: ${error.message}")
            }

        })
    }

    private fun banners(lists: MutableList<SliderItems>){
        binding.viewPager2.adapter = SliderAdapter(lists,binding.viewPager2)
        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = false
        binding.viewPager2.offscreenPageLimit = 3

        // Bảo vệ khỏi lỗi getChildAt(0) nếu Adapter rỗng
        if (binding.viewPager2.childCount > 0) {
            binding.viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }


        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer ( MarginPageTransformer(40))
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }
        }

        binding.viewPager2.setPageTransformer(compositePageTransformer)
        binding.viewPager2.currentItem = 1
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandle.removeCallbacks(sliderRunnable)
                // Bắt đầu lại vòng lặp sau khi người dùng tương tác hoặc thay đổi trang
                sliderHandle.postDelayed(sliderRunnable, 3000)
            }
        })
    }


    private fun initUpcomming() {
        val db = database ?: return // Thoát nếu database null
        val myRef: DatabaseReference = db.getReference("Upcomming")
        binding.progressBarTopUpcoming.visibility= View.VISIBLE
        val items = ArrayList<Film>()

        myRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(i in snapshot.children){
                        val item = i.getValue(Film::class.java)
                        if(item!=null){
                            items.add(item)
                        }
                    }

                    if(items.isNotEmpty()){
                        binding.recyclerViewUpcoming.layoutManager=
                            LinearLayoutManager(
                                this@MainActivity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        binding.recyclerViewUpcoming.adapter = FilmListAdapter(items)
                    }
                    binding.progressBarTopUpcoming.visibility= View.GONE

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Sửa lỗi TODO: Ghi log thay vì để trống
                Log.e("Firebase", "Lỗi tải Upcoming Movies: ${error.message}")
            }

        })
    }

    // Thêm các hàm lifecycle để quản lý vòng lặp slider an toàn
    override fun onPause() {
        super.onPause()
        sliderHandle.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        // Chỉ chạy nếu có adapter và item để tránh crash
        if (binding.viewPager2.adapter != null && binding.viewPager2.adapter!!.itemCount > 0) {
            sliderHandle.postDelayed(sliderRunnable, 3000)
        }
    }
}