package com.practicum.playlistmaker.presentation.search

import android.os.Handler
import android.os.Looper
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
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    
    companion object {
        private const val SEARCH_DEBOUNCE_DELAY_MS = 2000L
    }

    fun onQueryChanged(text: String) {
        lastQuery = text
        
        // Отменяем предыдущий поиск
        searchRunnable?.let { handler.removeCallbacks(it) }
        
        if (text.isBlank()) {
            _state.value = SearchState.History(interactor.history())
            return
        }
        
        // Устанавливаем новый поиск с задержкой
        searchRunnable = Runnable {
            _state.value = SearchState.Loading
            interactor.search(text) { result ->
                // Проверяем, что запрос не изменился
                if (text == lastQuery) {
                    result.onSuccess { list ->
                        _state.postValue(if (list.isEmpty()) SearchState.Empty else SearchState.Content(list))
                    }.onFailure {
                        _state.postValue(SearchState.Error)
                    }
                }
            }
        }
        handler.postDelayed(searchRunnable!!, SEARCH_DEBOUNCE_DELAY_MS)
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
    
    override fun onCleared() {
        super.onCleared()
        searchRunnable?.let { handler.removeCallbacks(it) }
    }
}
