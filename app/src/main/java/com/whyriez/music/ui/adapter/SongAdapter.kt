package com.whyriez.music.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.whyriez.music.R
import com.whyriez.music.databinding.ItemSongBinding
import com.whyriez.music.domain.model.Song

class SongAdapter(
    private val onSongClick: (Song, Int) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    private var activePlayingTrackId: Long? = null
    private var isCurrentlyPlaying: Boolean = false

    fun updatePlaybackState(trackId: Long?, isPlaying: Boolean) {
        if (this.activePlayingTrackId != trackId || this.isCurrentlyPlaying != isPlaying) {
            this.activePlayingTrackId = trackId
            this.isCurrentlyPlaying = isPlaying
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun onViewRecycled(holder: SongViewHolder) {
        super.onViewRecycled(holder)
        holder.stopAnimation()
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song, position: Int) {
            binding.titleSong.text = song.trackName
            binding.artistSong.text = song.artistName
            binding.albumSong.text = song.collectionName

            Glide.with(binding.imgSong)
                .load(song.artworkUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.ic_android_black_24dp)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imgSong)

            val isThisSongPlaying = (song.trackId == activePlayingTrackId)

            if (isThisSongPlaying) {
                binding.spectrumView.visibility = View.VISIBLE
                if (isCurrentlyPlaying) {
                    binding.spectrumView.startSimulation()
                } else {
                    binding.spectrumView.stopSimulation()
                }
                binding.titleSong.setTextColor(Color.parseColor("#1DB954"))
            } else {
                binding.spectrumView.stopSimulation()
                binding.spectrumView.visibility = View.GONE
                binding.titleSong.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.black)
                )
            }

            binding.root.setOnClickListener {
                onSongClick(song, position)
            }
        }

        fun stopAnimation() {
            binding.spectrumView.stopSimulation()
        }
    }

    private class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean =
            oldItem.trackId == newItem.trackId

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
            oldItem == newItem
    }
}