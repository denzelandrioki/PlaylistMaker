package com.practicum.playlistmaker.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/** Нижний inset [WindowInsetsCompat.Type.systemBars] как padding, чтобы контент не уходил под навигацию. */
fun View.applySystemBarBottomInsetAsPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
        v.updatePadding(bottom = bottom)
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
