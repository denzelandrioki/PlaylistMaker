package com.practicum.playlistmaker.presentation.search

import android.text.Editable
import android.text.TextWatcher

class SimpleTextWatcher(private val on: (String) -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable?) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        on(s?.toString().orEmpty())
    }
}
