package com.example.freemusic.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.freemusic.R
import com.example.freemusic.databinding.ItemListMusicInPlaylistBinding
import com.example.freemusic.model.FreeMusic
import com.squareup.picasso.Picasso

class AdapterListMusicInPlaylist(
    private val context: Context,
    private val listMusic: MutableList<FreeMusic>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<AdapterListMusicInPlaylist.ViewHolder>() {

    class ViewHolder(binding: ItemListMusicInPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imgMusic = binding.imgAvatarMusic
        val tvNameMusic = binding.tvNameMusic
        val tvArtistsMusic = binding.tvArtistMusic
        val btnMore = binding.btnMore
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListMusicInPlaylistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (listMusic[position].uri != null) {
            holder.tvNameMusic.text = listMusic[position].name
            holder.tvArtistsMusic.text = listMusic[position].artists_names
            listMusic[position].uri?.let {
                if (getBitmapByUri(context, it) != null) {
                    holder.imgMusic.setImageBitmap(getBitmapByUri(context, it))
                } else {
                    holder.imgMusic.setImageResource(R.drawable.ic_launcher_background)
                }
            }
        } else {
            Picasso.get().load(listMusic[position].thumbnail).into(holder.imgMusic)
            holder.tvNameMusic.text = listMusic[position].name
            holder.tvArtistsMusic.text = listMusic[position].artists_names
        }
        holder.btnMore.setOnClickListener {
        }
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return listMusic.size
    }

    private fun getBitmapByUri(context: Context, audioUri: Uri): Bitmap? {
        try {
            val mmr = MediaMetadataRetriever()
            val art: Bitmap
            val bfo = BitmapFactory.Options()
            mmr.setDataSource(context, audioUri)
            val rawArt: ByteArray? = mmr.embeddedPicture
            return if (null != rawArt) {
                art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
                art
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_background)
            }
        } catch (exception: Exception) {
            Log.i("errConvertUriToBitmap", exception.toString())
            return BitmapFactory.decodeResource(
                context.resources,
                R.drawable.ic_baseline_favorite_24_fill
            )
        }
    }

}