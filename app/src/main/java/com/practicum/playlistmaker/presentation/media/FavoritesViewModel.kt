package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.interactor.FavoritesInteractor
<<<<<<< Updated upstream
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Состояние экрана «Избранные треки»: заглушка или список. */
sealed interface FavoritesState {
    data object Empty : FavoritesState
    data class Content(val tracks: List<Track>) : FavoritesState
=======
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
>>>>>>> Stashed changes
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
