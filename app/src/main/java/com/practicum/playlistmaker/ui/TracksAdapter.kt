package com.practicum.playlistmaker.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.model.Track

class TracksAdapter (
    private val tracks: List<Track>,
    private val onClick: (Track) -> Unit = {}

): RecyclerView.Adapter<TrackViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
       return tracks.size
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
        holder.itemView.setOnClickListener { onClick(tracks[position]) }
    }
}