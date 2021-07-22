package com.example.freemusic.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.freemusic.R
import com.example.freemusic.databinding.ItemInListPlayingBinding
import com.example.freemusic.databinding.ItemListMusicPlayingBinding
import com.example.freemusic.model.FreeMusic
import com.squareup.picasso.Picasso

class AdapterListPlaying(
    private val context: Context,
    private val listMusic: MutableList<FreeMusic>,
    private val onItemClick: (Int) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var positionPlaying = -1

    private val typePlaying = 1
    private val typeNormal = 2

    private class ViewHolderPlaying(binding: ItemListMusicPlayingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imgMusic = binding.imgAvatarMusic
        val tvNameMusic = binding.tvNameMusic
        val tvArtistsMusic = binding.tvArtistMusic
        val btnMore = binding.btnMore
    }

    private class ViewHolder(binding: ItemInListPlayingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tvNumberOrder = binding.tvNumberOrder
        val imgMusic = binding.imgAvatarMusic
        val tvNameMusic = binding.tvNameMusic
        val tvArtistsMusic = binding.tvArtistMusic
        val btnMore = binding.btnMore
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            typePlaying -> ViewHolderPlaying(
                ItemListMusicPlayingBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            else -> ViewHolder(
                ItemInListPlayingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            typeNormal -> {
                val holderNormal = holder as ViewHolder
                if (listMusic[position].uri != null) {
                    holderNormal.tvNameMusic.text = listMusic[position].name
                    holderNormal.tvArtistsMusic.text = listMusic[position].artists_names
                    holderNormal.tvNumberOrder.text = "${position + 1}"
                    listMusic[position].uri?.let {
                        if (getBitmapByUri(context, it) != null) holder.imgMusic.setImageBitmap(
                            getBitmapByUri(context, it)
                        )
                        else holder.imgMusic.setImageResource(R.drawable.ic_launcher_background)
                    }
                } else {
                    holderNormal.tvNameMusic.text = listMusic[position].name
                    holderNormal.tvArtistsMusic.text = listMusic[position].artists_names
                    holderNormal.tvNumberOrder.text = "${position + 1}"
                    Picasso.get().load(listMusic[position].thumbnail).into(holderNormal.imgMusic)
                }
                holderNormal.itemView.setOnClickListener {
                    onItemClick(position)
                }
            }
            typePlaying -> {
                val holderPlaying = holder as ViewHolderPlaying
                if (listMusic[position].uri != null) {
                    holderPlaying.tvNameMusic.text = listMusic[position].name
                    holderPlaying.tvArtistsMusic.text = listMusic[position].artists_names
                    listMusic[position].uri?.let {
                        holder.imgMusic.setImageBitmap(getBitmapByUri(context, it))
                    }
                } else {
                    holderPlaying.tvNameMusic.text = listMusic[position].name
                    holderPlaying.tvArtistsMusic.text = listMusic[position].artists_names
                    Picasso.get().load(listMusic[position].thumbnail).into(holderPlaying.imgMusic)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listMusic.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            positionPlaying -> typePlaying
            else -> typeNormal
        }
    }

    fun setPositionPlaying(position: Int) {
        positionPlaying = position
    }

    private fun getBitmapByUri(context: Context, audioUri: Uri): Bitmap? {
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
    }
}