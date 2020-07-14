package ru.iandreyshev.musicplayer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_track.view.*
import ru.iandreyshev.musicplayer.R
import ru.iandreyshev.musicplayer.utils.adapterPositionSafely
import ru.iandreyshev.player.PlayerTrack

class TrackListAdapter : ListAdapter<PlayerTrack, TrackViewHolder>(DiffCallback) {

    private var mListener: (position: Int) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
            .let { view ->
                TrackViewHolder(view).apply {
                    itemView.clickableArea.setOnClickListener {
                        adapterPositionSafely(mListener)
                    }
                }
            }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        getItem(position)?.let { track ->
            holder.itemView.name.text = track.title
            holder.itemView.author.text = track.artist
            holder.itemView.thumbnail.setImageResource(track.iconRes)
        }
    }

    fun setOnClickListener(listener: (position: Int) -> Unit) {
        mListener = listener
    }

}

class TrackViewHolder(v: View) : RecyclerView.ViewHolder(v)

object DiffCallback : DiffUtil.ItemCallback<PlayerTrack>() {
    override fun areItemsTheSame(oldItem: PlayerTrack, newItem: PlayerTrack): Boolean = false
    override fun areContentsTheSame(oldItem: PlayerTrack, newItem: PlayerTrack): Boolean = false
}
