package com.practicum.playlistmaker.presentation.search

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.app.Creator
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.presentation.player.PlayerActivity
import com.practicum.playlistmaker.presentation.search.TracksAdapter

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var historyAdapter: TracksAdapter

    private val interactor by lazy { Creator.searchInteractor(this) }

    private var searchText: String? = null
    private var lastSearchQuery: String? = null

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    private val clickHandler = Handler(Looper.getMainLooper())
    private var clickAllowed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: MaterialToolbar = binding.searchToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        savedInstanceState?.let { searchText = it.getString(KEY_SEARCH_TEXT) }
        binding.searchEditText.setText(searchText)

        tracksAdapter = TracksAdapter(mutableListOf()) { onTrackClickedSafe(it) }
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tracksRecyclerView.adapter = tracksAdapter

        historyAdapter = TracksAdapter(mutableListOf()) { onTrackClickedSafe(it) }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

        binding.searchEditText.addTextChangedListener(textWatcher)
        binding.searchEditText.setOnFocusChangeListener { _, _ -> updateHistoryVisibility() }

        binding.clearButton.setOnClickListener {
            binding.searchEditText.text?.clear()
            binding.searchEditText.requestFocus()
            clearSearchResultViews()
            updateHistoryVisibility()
        }

        binding.clearHistoryButton.setOnClickListener {
            interactor.clearHistory()
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

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        updateHistoryVisibility()
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            searchText = s?.toString()
            binding.clearButton.isVisible = !s.isNullOrEmpty()
            updateHistoryVisibility()

            searchRunnable?.let { searchHandler.removeCallbacks(it) }
            val query = s?.toString()?.trim().orEmpty()
            if (query.isNotEmpty()) {
                searchRunnable = Runnable { performSearch(query) }
                searchHandler.postDelayed(searchRunnable!!, SEARCH_DEBOUNCE_MS)
            } else {
                clearSearchResultViews()
                binding.progressBar.visibility = View.GONE
            }
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun onTrackClicked(track: Track) {
        interactor.pushToHistory(track)
        Toast.makeText(this, "${track.trackName} — ${track.artistName}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, PlayerActivity::class.java)
            .putExtra(PlayerActivity.EXTRA_TRACK, track)
        startActivity(intent)
    }

    private fun onTrackClickedSafe(track: Track) {
        if (!clickAllowed) return
        clickAllowed = false
        clickHandler.postDelayed({ clickAllowed = true }, CLICK_DEBOUNCE_MS)
        onTrackClicked(track)
    }

    private fun updateHistoryVisibility() {
        val show = binding.searchEditText.hasFocus()
                && binding.searchEditText.text.isNullOrEmpty()
                && interactor.history().isNotEmpty()

        binding.historyGroup.isVisible = show
        if (show) {
            historyAdapter.setData(interactor.history())
            binding.tracksRecyclerView.visibility = View.GONE
            binding.emptyPlaceholder.visibility = View.GONE
            binding.errorPlaceholder.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchText)
    }

    override fun onDestroy() {
        binding.searchEditText.removeTextChangedListener(textWatcher)
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        clickHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun performSearch(query: String) {
        lastSearchQuery = query
        clearSearchResultViews()
        binding.progressBar.visibility = View.VISIBLE

        interactor.search(query) { result ->
            if (isFinishing || isDestroyed) return@search
            binding.progressBar.visibility = View.GONE
            result.onSuccess { tracks ->
                if (tracks.isEmpty()) showEmptyPlaceholder() else showTracks(tracks)
            }.onFailure {
                showErrorPlaceholder()
            }
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
        private const val SEARCH_DEBOUNCE_MS = 2_000L
        private const val CLICK_DEBOUNCE_MS = 1_000L
    }

    override fun onResume() {
        super.onResume()
        updateHistoryVisibility()
    }
}
