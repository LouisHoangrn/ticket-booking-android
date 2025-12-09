package com.finalexam.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finalexam.project.databinding.ViewholderGenreBinding

class GenreEachFilmAdapter(private val items: List<String>) :
    RecyclerView.Adapter<GenreEachFilmAdapter.Viewholder>() {

    class Viewholder(val binding: ViewholderGenreBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GenreEachFilmAdapter.Viewholder {
        val binding = ViewholderGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        holder.binding.titleTxt.text = items[position]
    }

    override fun getItemCount(): Int = items.size
}

