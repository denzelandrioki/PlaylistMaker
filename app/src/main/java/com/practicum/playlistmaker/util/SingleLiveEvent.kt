package com.practicum.playlistmaker.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * LiveData для one-time событий (навигация, Toast и т.д.).
 * Событие доставляется только один раз и сбрасывается после обработки,
 * чтобы не срабатывать повторно при перевороте экрана или повторной подписке.
 */
class SingleLiveEvent<T> : MutableLiveData<T?>() {

    /** Подписка на событие: [onEvent] вызовется один раз, после чего событие очищается. */
    fun observeEvent(owner: LifecycleOwner, onEvent: (T) -> Unit) {
        observe(owner, Observer { value ->
            value?.let {
                onEvent(it)
                postValue(null)
            }
        })
    }
}
