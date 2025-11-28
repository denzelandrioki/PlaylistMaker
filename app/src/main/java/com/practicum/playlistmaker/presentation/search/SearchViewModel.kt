package com.practicum.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.interactor.SearchInteractor

sealed interface SearchState {
    data object Idle : SearchState
    data object Loading : SearchState
    data class Content(val items: List<Track>) : SearchState
    data object Empty : SearchState
    data object Error : SearchState
    data class History(val items: List<Track>) : SearchState
}

class SearchViewModel(
    private val interactor: SearchInteractor
) : ViewModel() {

    private val _state = MutableLiveData<SearchState>(SearchState.History(interactor.history()))
    val state: LiveData<SearchState> = _state

    private var lastQuery: String? = null

    fun onQueryChanged(text: String) {
        lastQuery = text
        if (text.isBlank()) {
            _state.value = SearchState.History(interactor.history())
            return
        }
        _state.value = SearchState.Loading
        interactor.search(text) { result ->
            result.onSuccess { list ->
                _state.postValue(if (list.isEmpty()) SearchState.Empty else SearchState.Content(list))
            }.onFailure {
                _state.postValue(SearchState.Error)
            }
        }
    }

    fun onRetry() {
        lastQuery?.let { onQueryChanged(it) }
    }

    fun onClearHistory() {
        interactor.clearHistory()
        _state.value = SearchState.History(emptyList())
    }

    fun onClickTrack(track: Track) {
        interactor.pushToHistory(track)
    }
}
