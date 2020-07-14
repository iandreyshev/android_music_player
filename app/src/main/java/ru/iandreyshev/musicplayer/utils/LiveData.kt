package ru.iandreyshev.musicplayer.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations

fun <A, B, C> LiveData<A>.combineLatest(
    b: LiveData<B>,
    combineFunc: (A, B) -> C
): LiveData<C> = Transformations.map(combineLatest(b)) {
    combineFunc(it.first, it.second)
}

fun <A, B> LiveData<A>.combineLatest(b: LiveData<B>): LiveData<Pair<A, B>> {
    return MediatorLiveData<Pair<A, B>>().apply {
        var lastA: A? = null
        var lastB: B? = null

        addSource(this@combineLatest) {
            if (it == null && value != null) value = null
            lastA = it
            if (lastA != null && lastB != null) value = lastA!! to lastB!!
        }

        addSource(b) {
            if (it == null && value != null) value = null
            lastB = it
            if (lastA != null && lastB != null) value = lastA!! to lastB!!
        }
    }
}
