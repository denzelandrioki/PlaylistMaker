package com.practicum.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.Instance.NetworkClient
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.dto.toDomain
import com.practicum.playlistmaker.model.PlaylistApiResponse
import com.practicum.playlistmaker.model.Track
import com.practicum.playlistmaker.ui.TracksAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var historyAdapter: TracksAdapter
    private lateinit var history: SearchHistory
    private var searchText: String? = null
    private var lastSearchQuery: String? = null
    private var runningCall: Call<PlaylistApiResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        val toolbar = binding.searchToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // История — ДОЛЖНА быть до первого использования!
        history = SearchHistory(getSharedPreferences(SearchHistory.PREFS_NAME, MODE_PRIVATE))

        // Восстановим текст
        savedInstanceState?.let { searchText = it.getString(KEY_SEARCH_TEXT) }
        binding.searchEditText.setText(searchText)

        // Адаптеры
        tracksAdapter = TracksAdapter(mutableListOf()) { onTrackClicked(it) }
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tracksRecyclerView.adapter = tracksAdapter

        historyAdapter = TracksAdapter(mutableListOf()) { onTrackClicked(it) }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

        // Листенеры (по одному разу)
        binding.searchEditText.addTextChangedListener(textWatcher)
        binding.searchEditText.setOnFocusChangeListener { _, _ -> updateHistoryVisibility() }

        binding.clearButton.setOnClickListener {
            binding.searchEditText.text?.clear()
            binding.searchEditText.clearFocus()
            hideKeyboard()
            clearSearchResultViews()
            updateHistoryVisibility()
        }

        binding.clearHistoryButton.setOnClickListener {
            history.clear()
            updateHistoryVisibility()
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                    hideKeyboard()
                }
                true
            } else false
        }

        binding.retryButton.setOnClickListener { lastSearchQuery?.let { performSearch(it) } }

        // Показать историю сразу при входе (без всплытия клавиатуры)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        if (binding.searchEditText.text.isNullOrEmpty() && history.notEmpty()) {
            binding.searchEditText.requestFocus()
            binding.searchEditText.post { updateHistoryVisibility() }
        } else {
            updateHistoryVisibility()
        }
    }


    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            searchText = s?.toString()
            binding.clearButton.isVisible = !s.isNullOrEmpty()
            updateHistoryVisibility()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun onTrackClicked(track: Track) {
        history.add(track)                     // <— сохранить/поднять в историю
        Toast.makeText(this, "${track.trackName} — ${track.artistName}", Toast.LENGTH_SHORT).show()
        // TODO: В следующем спринте — переход в плеер
    }


    private fun updateHistoryVisibility() {
        val show = binding.searchEditText.hasFocus()
                && binding.searchEditText.text.isNullOrEmpty()
                && history.notEmpty()

        binding.historyGroup.isVisible = show
        if (show) {
            historyAdapter.setData(history.get())
            // Когда открыта история — прячем результаты/плейсхолдеры
            binding.tracksRecyclerView.visibility = View.GONE
            binding.emptyPlaceholder.visibility = View.GONE
            binding.errorPlaceholder.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchText)
    }




    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(KEY_SEARCH_TEXT)
        binding.searchEditText.setText(searchText)
    }

    override fun onDestroy() {
        binding.searchEditText.removeTextChangedListener(textWatcher)
        super.onDestroy()
    }

    private fun performSearch(query: String) {
        lastSearchQuery = query
        clearSearchResultViews()
        runningCall?.cancel()
        runningCall = NetworkClient.itunesApi.searchTracks(query).also { call ->
            call.enqueue(object : Callback<PlaylistApiResponse> {
                override fun onResponse(
                    c: Call<PlaylistApiResponse>,
                    response: Response<PlaylistApiResponse>
                ) {
                    if (!isFinishing && !isDestroyed) {
                        if (response.isSuccessful && response.body() != null) {
                            val tracks = response.body()!!.results?.mapNotNull { it.toDomain() }.orEmpty()
                            if (tracks.isEmpty()) showEmptyPlaceholder() else showTracks(tracks)
                        } else showErrorPlaceholder()
                    }
                }
                override fun onFailure(c: Call<PlaylistApiResponse>, t: Throwable) {
                    if (c.isCanceled) return
                    if (!isFinishing && !isDestroyed) showErrorPlaceholder()
                }
            })
        }
    }

    private fun showTracks(tracks: List<Track>) {
        binding.historyGroup.visibility = View.GONE
        binding.emptyPlaceholder.visibility = View.GONE
        binding.errorPlaceholder.visibility = View.GONE
        binding.tracksRecyclerView.visibility = View.VISIBLE
        tracksAdapter.setData(tracks)
    }

    private fun showEmptyPlaceholder() {
        binding.tracksRecyclerView.visibility = View.GONE
        binding.errorPlaceholder.visibility = View.GONE
        binding.emptyPlaceholder.visibility = View.VISIBLE
    }

    private fun showErrorPlaceholder() {
        binding.tracksRecyclerView.visibility = View.GONE
        binding.emptyPlaceholder.visibility = View.GONE
        binding.errorPlaceholder.visibility = View.VISIBLE
    }

    private fun clearSearchResultViews() {
        tracksAdapter.setData(emptyList())
        binding.tracksRecyclerView.visibility = View.GONE
        binding.emptyPlaceholder.visibility = View.GONE
        binding.errorPlaceholder.visibility = View.GONE
    }

    companion object {
        private const val KEY_SEARCH_TEXT = "KEY_SEARCH_TEXT"
    }


    override fun onResume() {
        super.onResume()
        updateHistoryVisibility()
    }
}
