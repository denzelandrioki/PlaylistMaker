package com.practicum.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.interactor.SearchInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 2_000L

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

    private var lastQuery: String = ""
    private var debounceJob: Job? = null

    fun onQueryChanged(q: String) {
        lastQuery = q
        if (q.isBlank()) {
            _state.value = SearchState.History(interactor.history())
            return
        }
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            performSearch(q)
        }
    }

    fun onRetry() {
        if (lastQuery.isNotBlank()) performSearch(lastQuery)
    }

    fun onClickTrack(track: Track) {
        interactor.pushToHistory(track)
    }

    fun onClearHistory() {
        interactor.clearHistory()
        _state.value = SearchState.History(emptyList())
    }

    private fun performSearch(q: String) {
        _state.value = SearchState.Loading
        interactor.search(q) { result ->
            result.onSuccess { list ->
                _state.postValue(if (list.isEmpty()) SearchState.Empty else SearchState.Content(list))
            }.onFailure {
                _state.postValue(SearchState.Error)
            }
        }
    }
}
