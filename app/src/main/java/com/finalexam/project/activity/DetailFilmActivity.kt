package com.finalexam.project.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.finalexam.project.adapter.CastListAdapter
import com.finalexam.project.adapter.GenreEachFilmAdapter
import com.finalexam.project.databinding.ActivityDetailFilmBinding
import com.finalexam.project.model.Film
import eightbitlab.com.blurview.RenderScriptBlur     // 🔥 IMPORT ĐÚNG THƯ VIỆN BLURVIEW

class DetailFilmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailFilmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailFilmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setVariable()
    }

    private fun setVariable() {
        val film = intent.getSerializableExtra("object") as Film

        val requestOptions = RequestOptions().transform(
            CenterCrop(),
            GranularRoundedCorners(0f, 0f, 50f, 50f)
        )

        Glide.with(this)
            .load(film.Poster)
            .apply(RequestOptions().transform(CenterCrop()))
            .into(binding.filmPic)

        binding.titleTxt.text = film.Title
        binding.imdbTxt.text = "IMDB ${film.Imdb}"
        binding.movieTimeTxt.text = "${film.Year} - ${film.time}"
        binding.movieSummeryTxt.text = film.Description

        binding.backBtn.setOnClickListener {
            finish()
        }

        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = decorView.background

        binding.blurView.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(this))
            .setBlurRadius(18f)
            .setHasFixedTransformationMatrix(true)

        binding.blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.blurView.clipToOutline = true

        film.Genre?.let {
            binding.genreView.adapter = GenreEachFilmAdapter(it)
            binding.genreView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }

        film.Casts?.let {
            binding.castListView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.castListView.adapter = CastListAdapter(it)
        }


        binding.buyTicketBtn.setOnClickListener {
            val intent = Intent(this, SeatListActivity::class.java)
            intent.putExtra("film", film)
            startActivity(intent)
        }
    }
}
