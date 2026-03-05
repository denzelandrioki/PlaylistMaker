package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.interactor.FavoritesInteractor
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FavoritesViewModel(
    private val favorites: FavoritesInteractor,
) : ViewModel() {

    private val _list = MutableLiveData<List<Track>>(emptyList())
    val list: LiveData<List<Track>> = _list

    init {
        favorites.getFavorites()
            .catch { }
            .onEach { _list.postValue(it) }
            .launchIn(viewModelScope)
    }
}
