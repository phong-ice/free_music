package com.example.freemusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.freemusic.R
import com.example.freemusic.databinding.ItemListMusicBinding
import com.example.freemusic.model.FreeMusic
import com.squareup.picasso.Picasso

class AdapterListTopMusic(
    private val listTopMusic: MutableList<FreeMusic>,
    private val onItemClick: (Int) -> Unit,
    private val addMusicToFavoriteList: (FreeMusic, Int) -> Unit
) : RecyclerView.Adapter<AdapterListTopMusic.ViewHolder>() {

    class ViewHolder(private val binding: ItemListMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imgAvatarMusic = binding.imgAvatarMusic
        val tvNameMusic = binding.tvNameMusic
        val tvArtistMusic = binding.tvArtistMusic
        val btnFavoriteMusic = binding.btnFavorite
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListMusicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get().load(listTopMusic[position].thumbnail).into(holder.imgAvatarMusic)
        holder.tvNameMusic.text = listTopMusic[position].name
        holder.tvArtistMusic.text = listTopMusic[position].artists_names
        when (listTopMusic[position].favorite) {
            true -> holder.btnFavoriteMusic.setImageResource(R.drawable.ic_baseline_favorite_24_fill)
            false -> holder.btnFavoriteMusic.setImageResource(R.drawable.ic_baseline_favorite_24)
        }
        holder.btnFavoriteMusic.setOnClickListener {
            if (listTopMusic[position].favorite) {
                listTopMusic[position].favorite = false
                holder.btnFavoriteMusic.setImageResource(R.drawable.ic_baseline_favorite_24)
            } else {
                listTopMusic[position].favorite = true
                holder.btnFavoriteMusic.setImageResource(R.drawable.ic_baseline_favorite_24_fill)
            }
            addMusicToFavoriteList(listTopMusic[position], position)
        }
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return listTopMusic.size
    }
}