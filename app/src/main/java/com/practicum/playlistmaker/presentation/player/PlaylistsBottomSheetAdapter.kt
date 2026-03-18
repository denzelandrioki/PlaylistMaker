package com.practicum.playlistmaker.presentation.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ItemPlaylistBottomSheetBinding
import com.practicum.playlistmaker.domain.entity.Playlist
import java.io.File

class PlaylistsBottomSheetAdapter(
    private val onPlaylistClick: (Playlist) -> Unit,
) : ListAdapter<Playlist, PlaylistsBottomSheetAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlaylistBottomSheetBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onPlaylistClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemPlaylistBottomSheetBinding,
        private val onPlaylistClick: (Playlist) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlistTitle.text = playlist.name
            binding.playlistTrackCount.text = formatTrackCount(binding.root.context, playlist.trackCount)
            if (!playlist.coverPath.isNullOrBlank()) {
                val file = File(playlist.coverPath)
                if (file.exists()) {
                    Glide.with(binding.root).load(file).centerCrop().into(binding.playlistCover)
                } else {
                    binding.playlistCover.setImageResource(R.drawable.img_placeholder)
                }
            } else {
                binding.playlistCover.setImageResource(R.drawable.img_placeholder)
            }
            binding.root.setOnClickListener { onPlaylistClick(playlist) }
        }

        private fun formatTrackCount(context: android.content.Context, count: Int): String {
            val mod10 = count % 10
            val mod100 = count % 100
            val format = when {
                mod100 in 11..14 -> R.string.tracks_count_many
                mod10 == 1 -> R.string.tracks_count_one
                mod10 in 2..4 -> R.string.tracks_count_few
                else -> R.string.tracks_count_many
            }
            return context.getString(format, count)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(a: Playlist, b: Playlist) = a.id == b.id
        override fun areContentsTheSame(a: Playlist, b: Playlist) = a == b
    }
}
