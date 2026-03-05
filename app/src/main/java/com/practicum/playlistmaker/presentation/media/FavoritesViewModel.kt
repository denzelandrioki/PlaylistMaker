package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.interactor.FavoritesInteractor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Состояние экрана «Избранные треки»: заглушка или список. */
sealed interface FavoritesState {
    data object Empty : FavoritesState
    data class Content(val tracks: List<Track>) : FavoritesState
}

class FavoritesViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val _state = MutableLiveData<FavoritesState>(FavoritesState.Empty)
    val state: LiveData<FavoritesState> = _state

    init {
        viewModelScope.launch {
            favoritesInteractor.getFavorites().collect { list ->
                _state.value = if (list.isEmpty()) FavoritesState.Empty else FavoritesState.Content(list)
            }
        }
    }
}
