package ru.iandreyshev.musicplayer.utils

import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.ViewHolder.adapterPositionSafely(action: (position: Int) -> Unit) {
    when (val position = adapterPosition) {
        RecyclerView.NO_POSITION -> Unit
        else -> action(position)
    }
}
