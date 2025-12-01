package com.finalexam.project.activity

import android.app.slice.SliceItem
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
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
import kotlin.jvm.java
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private val sliderHandle = Handler()

    private val sliderRunnable = Runnable {
        binding.viewPager2.currentItem=binding.viewPager2.currentItem + 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        initBanner()
        initTopMovies()
        initUpcomming()
    }

    private fun initTopMovies() {
        val myRef: DatabaseReference = database.getReference("Items")
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
                TODO("Not yet implemented")
            }

        })
    }

    private fun initBanner(){
        val myRef = database.getReference("Banners")
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
                TODO("Not yet implemented")
            }

        })
    }
    private fun banners(lists: MutableList<SliderItems>){
        binding.viewPager2.adapter = SliderAdapter(lists,binding.viewPager2)
        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = false
        binding.viewPager2.offscreenPageLimit = 3
        binding.viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer ( MarginPageTransformer(40))
            addTransformer { page, position ->
                var r=1 - Math.abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }
        }

        binding.viewPager2.setPageTransformer(compositePageTransformer)
        binding.viewPager2.currentItem = 1
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandle.removeCallbacks(sliderRunnable)
            }
        })

    }


    private fun initUpcomming() {
        val myRef: DatabaseReference = database.getReference("Upcomming")
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
                TODO("Not yet implemented")
            }

        })
    }
}