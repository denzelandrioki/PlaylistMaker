package com.practicum.playlistmaker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.model.Track

class TrackViewHolder private constructor(itemView: View) :
    RecyclerView.ViewHolder(itemView){

    private val cover       = itemView.findViewById<ImageView>(R.id.cover)
    private val trackName   = itemView.findViewById<TextView>(R.id.trackName)
    private val subtitle = itemView.findViewById<TextView>(R.id.subtitle)

    fun bind(track: Track) {
        trackName.text  = track.trackName
        subtitle.text   = "${track.artistName} · ${track.trackTime}"



        val radius = itemView.resources.getDimensionPixelSize(R.dimen.track_cover_radius)
        Glide.with(itemView)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.img_placeholder)
            .error(R.drawable.img_placeholder)
            .transform(RoundedCorners(radius))
            .into(cover)
    }

    companion object {
        fun create(parent: ViewGroup): TrackViewHolder =
            TrackViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_track, parent, false)
            )
    }

}