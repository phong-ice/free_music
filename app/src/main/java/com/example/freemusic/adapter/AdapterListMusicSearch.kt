package com.example.freemusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.freemusic.databinding.ItemListMusicSearchBinding
import com.example.freemusic.model.FreeMusic
import com.squareup.picasso.Picasso

class AdapterListMusicSearch(
    private val listMusicSearch: MutableList<FreeMusic>,
    private val onItemClick: (Int) -> Unit
) :
    RecyclerView.Adapter<AdapterListMusicSearch.ViewHolder>() {

    class ViewHolder(binding: ItemListMusicSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageMusic = binding.imgAvatarMusic
        val tvNameMusic = binding.tvNameMusic
        val tvArtistsMusic = binding.tvArtistMusic
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListMusicSearchBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get()
            .load(listMusicSearch[position].thumbnail)
            .into(holder.imageMusic)
        holder.tvArtistsMusic.text = listMusicSearch[position].artists_names
        holder.tvNameMusic.text = listMusicSearch[position].name
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return listMusicSearch.size
    }
}