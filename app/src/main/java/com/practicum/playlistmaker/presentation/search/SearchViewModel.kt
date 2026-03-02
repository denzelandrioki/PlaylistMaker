package com.practicum.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.domain.interactor.SearchInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Состояния экрана поиска: пусто/история → загрузка → контент/пусто/ошибка. */
sealed interface SearchState {
    data object Idle : SearchState
    data object Loading : SearchState
    data class Content(val items: List<Track>) : SearchState
    data object Empty : SearchState
    data object Error : SearchState
    data class History(val items: List<Track>) : SearchState
}

/**
 * ViewModel экрана поиска.
 * Дебаунс через корутины; запуск поиска и сбор Flow интерактора — в viewModelScope.
 */
class SearchViewModel(
    private val interactor: SearchInteractor
) : ViewModel() {

    private val _state = MutableLiveData<SearchState>(SearchState.History(interactor.history()))
    val state: LiveData<SearchState> = _state

    private var lastQuery: String? = null
    private var searchJob: Job? = null
    private var clickJob: Job? = null
    private var lastClickedTrack: Track? = null

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY_MS = 2000L
        private const val CLICK_DEBOUNCE_DELAY_MS = 300L
    }

    /** Дебаунс ввода; после задержки — запуск Flow поиска в viewModelScope. */
    fun onQueryChanged(text: String) {
        lastQuery = text
        searchJob?.cancel()

        if (text.isBlank()) {
            _state.value = SearchState.History(interactor.history())
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY_MS)
            val query = text
            if (query != lastQuery) return@launch
            _state.value = SearchState.Loading
            interactor.search(query)
                .catch { _state.postValue(SearchState.Error) }
                .collect { result ->
                    if (query != lastQuery) return@collect
                    result.onSuccess { list ->
                        _state.postValue(
                            if (list.isEmpty()) SearchState.Empty else SearchState.Content(list)
                        )
                    }.onFailure {
                        _state.postValue(SearchState.Error)
                    }
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

    /** Дебаунс нажатия: через 300 мс добавление трека в историю. */
    fun onClickTrack(track: Track) {
        lastClickedTrack = track
        clickJob?.cancel()
        clickJob = viewModelScope.launch {
            delay(CLICK_DEBOUNCE_DELAY_MS)
            lastClickedTrack?.let { interactor.pushToHistory(it) }
            lastClickedTrack = null
        }
    }

    override fun onCleared() {
        searchJob?.cancel()
        clickJob?.cancel()
        super.onCleared()
    }
}
