package com.practicum.playlistmaker.ui

import android.text.format.DateUtils
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
        val mmss = DateUtils.formatElapsedTime(track.trackTimeMillis / 1000)
        subtitle.text   = "${track.artistName} · $mmss"



        val radius = itemView.resources.getDimensionPixelSize(R.dimen.track_cover_radius)
        val coverUrl = track.artworkUrl100.replace("100x100bb.jpg", "512x512bb.jpg")
        Glide.with(itemView)
            .load(coverUrl)
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