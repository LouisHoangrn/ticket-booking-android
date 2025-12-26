package com.finalexam.project.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.finalexam.project.activity.DetailFilmActivity
import com.finalexam.project.databinding.ViewholderFilmBinding
import com.finalexam.project.model.Film

class FilmListAdapter(private val items: ArrayList<Film>):
RecyclerView.Adapter<FilmListAdapter.Viewholder>(){

    private var context: Context?=null
    inner class Viewholder(private val binding: ViewholderFilmBinding):
    RecyclerView.ViewHolder(binding.root){
        fun bind(item: Film){
            binding.nameTxt.text = item.Title
            val requestOption = RequestOptions()
                .transform(CenterCrop(), RoundedCorners(30))

            Glide.with(context!!)
                .load(item.Poster)
                .apply(requestOption)
                .into(binding.pic)

            binding.root.setOnClickListener {
                val intent = Intent(context, DetailFilmActivity::class.java)
                intent.putExtra("object", item)
                context!!.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FilmListAdapter.Viewholder {
        context = parent.context
        val binding = ViewholderFilmBinding.inflate(LayoutInflater.from(context),parent,false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: FilmListAdapter.Viewholder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

}