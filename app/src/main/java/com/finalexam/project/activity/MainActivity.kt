package com.finalexam.project.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.finalexam.project.R
import com.finalexam.project.adapter.FilmListAdapter
import com.finalexam.project.adapter.SliderAdapter
import com.finalexam.project.databinding.ActivityMainBinding
import com.finalexam.project.fragment.BookmarkFragment
import com.finalexam.project.fragment.CartFragment
import com.finalexam.project.model.Film
import com.finalexam.project.model.SliderItems
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore

import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // === THUỘC TÍNH TỪ PHIÊN BẢN HOME SCREEN (265 DÒNG) ===
    private var database: FirebaseDatabase? = null
    private val sliderHandle = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        // Logic kiểm tra giới hạn để tránh IndexOutOfBoundsException và reset vòng lặp
        if (binding.viewPager2.adapter != null && binding.viewPager2.currentItem < binding.viewPager2.adapter!!.itemCount - 1) {
            binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1
        } else {
            binding.viewPager2.currentItem = 0
        }
    }

    // === THUỘC TÍNH TỪ PHIÊN BẢN NAVIGATION (156 DÒNG) ===
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    var userId: String = "anonymous_user"
    var isAuthReady: Boolean = false
    private var currentFragmentTag: String = "" // Dùng để kiểm tra fragment hiện tại


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

        // === MERGED: 1. Khởi tạo Auth/Firestore và Lắng nghe trạng thái người dùng ===
        setupFirebase()

        // === MERGED: 2. Thiết lập Bottom Navigation (Home, Bookmark, Cart) ===
        setupBottomNavigation()

        // BƯỚC 3: Khởi tạo Firebase Realtime DB và Tải dữ liệu Home Screen
        database = runCatching {
            FirebaseDatabase.getInstance()
        }.onFailure { e ->
            Log.e("MainActivity", "CRITICAL: Firebase Realtime Database KHÔNG THỂ khởi tạo. Lỗi: ${e.message}", e)
        }.getOrNull()

        // BƯỚC 4: Cập nhật Profile và Tải dữ liệu
        updateProfileHeader()

        // Chỉ tải dữ liệu phim nếu Database được khởi tạo thành công
        if (database != null) {
            initBanner()
            initTopMovies()
            initUpcomming()
        } else {
            Log.w("MainActivity", "Firebase Database chưa khởi tạo. Không tải dữ liệu phim.")
        }

        // BƯỚC 5: Log out
        runCatching {
            binding.btnLogout.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }.onFailure { e ->
            // Đã loại bỏ View ID này trong layout chính, nhưng giữ lại logic nếu người dùng thêm vào sau
            Log.e("MainActivity", "LỖI: Không tìm thấy ID 'btnLogout' trong Layout. Lỗi: ${e.message}", e)
        }
    }

    // === PHƯƠNG THỨC XỬ LÝ AUTH/FIRESTORE (TỪ PHIÊN BẢN NAVIGATION) ===
    /**
     * Khởi tạo Firebase và thực hiện Xác thực ban đầu.
     */
    private fun setupFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            auth = Firebase.auth
            db = Firebase.firestore

            // Lắng nghe trạng thái Auth để xác định userId
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user != null) {
                    userId = user.uid
                    isAuthReady = true
                    Log.d("MainActivity", "Auth Ready. User ID: $userId")
                } else {
                    // Nếu không có token, thử đăng nhập ẩn danh
                    handleInitialSignIn(null)
                }
            }

            // Xử lý Custom Token (nếu có)
            // Cần đảm bảo __initial_auth_token là biến toàn cục được khởi tạo
            // trước khi truy cập nó trong môi trường Canvas.
            // Đoạn code này được giữ nguyên từ phiên bản trước.
            // val customToken = if (::__initial_auth_token.isInitialized) __initial_auth_token else null
            // if (!auth.currentUser?.isAnonymous!! && customToken != null) {
            //    handleInitialSignIn(customToken)
            // }

        } catch (e: Exception) {
            Log.e("MainActivity", "Lỗi khởi tạo Firebase: ${e.message}")
            Toast.makeText(this, "Lỗi kết nối Firebase.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Xử lý đăng nhập ban đầu bằng Custom Token hoặc Đăng nhập Ẩn danh.
     */
    private fun handleInitialSignIn(authToken: String?) {
        val signInTask = if (authToken != null && authToken.isNotEmpty()) {
            auth.signInWithCustomToken(authToken)
        } else {
            auth.signInAnonymously()
        }

        signInTask
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "Đăng nhập thành công. UID: ${auth.currentUser?.uid}")
                } else {
                    Log.w("MainActivity", "Đăng nhập thất bại.", task.exception)
                }
            }
    }

    /**
     * Thiết lập Bottom Navigation
     */
    private fun setupBottomNavigation() {
        // LƯU Ý: Không có fragment nào được tải mặc định, nội dung Home đã được tải
        // trực tiếp vào Activity nên không cần tải HomeFragment ở đây.

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // YÊU CẦU: NÚT HOME CHỈ QUAY VỀ TRANG MAINACTIVITY (TẢI LẠI)
                    Log.d("Navigation", "Nút Home được nhấn: Tải lại MainActivity.")

                    // Tạo Intent mới để khởi động lại MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish() // Đóng Activity hiện tại
                    true
                }
                R.id.nav_bookmark -> {
                    // YÊU CẦU: CHỈ HIỂN THỊ BOOKMARKFRAGMENT KHI BẤM NÚT
                    replaceFragment(BookmarkFragment(), BookmarkFragment::class.java.simpleName)
                    true
                }
                R.id.nav_cart -> {
                    // YÊU CẦU: CHỈ HIỂN THỊ CARTFRAGMENT KHI BẤM NÚT
                    replaceFragment(CartFragment(), CartFragment::class.java.simpleName)
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Thay thế Fragment hiện tại bằng Fragment mới.
     * Fragment này sẽ được tải vào R.id.fragment_container.
     */
    private fun replaceFragment(fragment: Fragment, tag: String) {
        // Nếu Fragment được chọn đã là Fragment hiện tại, không làm gì cả
        if (currentFragmentTag == tag) return

        currentFragmentTag = tag

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment, tag)
            commit()
        }
    }


    // === PHƯƠNG THỨC XỬ LÝ HOME SCREEN (TỪ PHIÊN BẢN 265 DÒNG) ===

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