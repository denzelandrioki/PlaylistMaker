package com.practicum.playlistmaker.presentation.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ItemPlaylistBinding
import com.practicum.playlistmaker.domain.entity.Playlist
import java.io.File

class PlaylistsAdapter(
    private val onPlaylistClick: (Playlist) -> Unit,
) : ListAdapter<Playlist, PlaylistsAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding, onPlaylistClick)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlaylistViewHolder(
        private val binding: ItemPlaylistBinding,
        private val onPlaylistClick: (Playlist) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlistTitle.text = playlist.name
            binding.playlistTrackCount.text = formatTrackCount(playlist.trackCount)
            if (!playlist.coverPath.isNullOrBlank()) {
                val file = File(playlist.coverPath)
                if (file.exists()) {
                    Glide.with(binding.root)
                        .load(file)
                        .centerCrop()
                        .into(binding.playlistCover)
                } else {
                    binding.playlistCover.setImageResource(R.drawable.img_placeholder)
                }
            } else {
                binding.playlistCover.setImageResource(R.drawable.img_placeholder)
            }
            binding.root.setOnClickListener { onPlaylistClick(playlist) }
        }

        private fun formatTrackCount(count: Int): String {
            val mod10 = count % 10
            val mod100 = count % 100
            val format = when {
                mod100 in 11..14 -> R.string.tracks_count_many
                mod10 == 1 -> R.string.tracks_count_one
                mod10 in 2..4 -> R.string.tracks_count_few
                else -> R.string.tracks_count_many
            }
            return binding.root.context.getString(format, count)
        }
    }

    private class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(a: Playlist, b: Playlist) = a.id == b.id
        override fun areContentsTheSame(a: Playlist, b: Playlist) = a == b
    }
}
