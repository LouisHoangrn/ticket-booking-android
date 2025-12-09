package com.finalexam.project.activity

import android.icu.text.DecimalFormat
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.finalexam.project.R
import com.finalexam.project.adapter.DateAdapter
import com.finalexam.project.adapter.SeatListAdapter
import com.finalexam.project.adapter.TimeAdapter
import com.finalexam.project.databinding.ActivitySeatListBinding
import com.finalexam.project.model.Film
import com.finalexam.project.model.Seat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SeatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeatListBinding
    private lateinit var film: Film
    private var price: Double = 0.0
    private var number: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentExtra()
        steVariable()
        initTimeDateList()
        initSeatsList()
    }

    private fun initSeatsList() {
        val gridLayoutManager = GridLayoutManager( this,  7)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position % 7 == 3) 1 else 1
            }
        }

        binding.apply {
            seatRecyclerview.layoutManager = gridLayoutManager

            val seatList = mutableListOf<Seat>()
            val numberSeats = 81
            for (i in 0 until numberSeats) {
                val seatName = ""
                val seatStatus =
                    if (i==2 || i==20 || i==33 || i==41 || i==50 || i==72 || i==73) Seat.SeatStatus.UNAVAILABLE
                    else Seat.SeatStatus.AVAILABLE
            seatList.add(Seat(seatStatus, seatName))
            }

            val seatAdapter= SeatListAdapter(seatList,this@SeatListActivity,object : SeatListAdapter.SelectedSeat {
                override fun Return(selectedName: String, num: Int) {
                    numberSelectedTxt.text="$num Seat Selected"
                    val df= DecimalFormat("#.##")
                    price=df.format(num*film.price).toDouble()
                    number=num
                    priceTxt.text="$$price"
                }

            })
            seatRecyclerview.adapter = seatAdapter
            seatRecyclerview.isNestedScrollingEnabled = false

        }
    }

    private fun initTimeDateList() {
        binding.apply {
            dateRecyclerview.layoutManager =
                LinearLayoutManager(this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
            dateRecyclerview.adapter = DateAdapter(generateDates())

            timeRecyclerview.layoutManager =
                LinearLayoutManager(this@SeatListActivity, LinearLayoutManager.HORIZONTAL, false)
            timeRecyclerview.adapter = TimeAdapter(generateTimeSlots())
        }
    }

    private fun steVariable() {
        binding.backBtn.setOnClickListener { finish() }
    }

    private fun getIntentExtra() {
        film = intent.getSerializableExtra("film") as Film
    }

    private fun generateDates(): List<String> {
        val dates = mutableListOf<String>()
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEE/dd/MMM")

        for (i in 0 until 7) {
            dates.add(today.plusDays(i.toLong()).format(formatter))
        }
        return dates
    }

    private fun generateTimeSlots(): List<String> {
        val timeSlots = mutableListOf<String>()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")

        for (i in 0 until 24 step 2) {
            val time = LocalDate.now().atTime(i, 0)
            timeSlots.add(time.format(formatter))
        }
        return timeSlots
    }
}