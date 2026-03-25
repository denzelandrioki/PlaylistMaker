package com.practicum.playlistmaker.presentation.playlist

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.presentation.search.TrackViewHolder

class PlaylistTracksAdapter(
    private val onTrackClick: (Track) -> Unit,
    private val onTrackLongClick: (Track) -> Unit,
) : ListAdapter<Track, RecyclerView.ViewHolder>(TrackDiffCallback()) {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        TrackViewHolder.create(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val track = getItem(position)
        (holder as TrackViewHolder).bind(track)
        holder.itemView.setOnClickListener { onTrackClick(track) }
        holder.itemView.setOnLongClickListener {
            onTrackLongClick(track)
            true
        }
    }

    private class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(a: Track, b: Track) = a.trackId == b.trackId
        override fun areContentsTheSame(a: Track, b: Track) = a == b
    }
}
