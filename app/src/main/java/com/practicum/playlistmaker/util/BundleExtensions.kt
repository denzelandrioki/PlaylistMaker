package com.practicum.playlistmaker.util

import android.os.Build
import android.os.Bundle
import android.os.Parcelable

/**
 * Безопасное получение Parcelable из Bundle с учётом API (TIRAMISU и выше — типизированный getParcelable).
 * Можно использовать в любом фрагменте или активности.
 */
@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, T::class.java)
    } else {
        getParcelable(key)
    }
