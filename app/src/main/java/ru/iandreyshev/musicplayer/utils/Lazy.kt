package ru.iandreyshev.musicplayer.utils

fun <T> uiLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)
