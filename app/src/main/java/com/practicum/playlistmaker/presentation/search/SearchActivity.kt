package com.practicum.playlistmaker.presentation.search

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.presentation.player.PlayerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModel()

    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var historyAdapter: TracksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: MaterialToolbar = binding.searchToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        tracksAdapter = TracksAdapter(mutableListOf()) { onTrackClicked(it) }
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tracksRecyclerView.adapter = tracksAdapter

        historyAdapter = TracksAdapter(mutableListOf()) { onTrackClicked(it) }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

        binding.clearButton.setOnClickListener { binding.searchEditText.text?.clear() }
        binding.clearHistoryButton.setOnClickListener { viewModel.onClearHistory() }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                viewModel.onQueryChanged(binding.searchEditText.text.toString())
                true
            } else false
        }

        binding.searchEditText.addTextChangedListener(SimpleTextWatcher { text ->
            binding.clearButton.isVisible = text.isNotEmpty()
            viewModel.onQueryChanged(text)
        })

        binding.retryButton.setOnClickListener { viewModel.onRetry() }

        viewModel.state.observe(this) { st ->
            when (st) {
                is SearchState.Loading -> { setAllGone(); binding.progressBar.isVisible = true }
                is SearchState.Content -> { setAllGone(); binding.tracksRecyclerView.isVisible = true; tracksAdapter.setData(st.items) }
                is SearchState.Empty -> { setAllGone(); binding.emptyPlaceholder.isVisible = true }
                is SearchState.Error -> { setAllGone(); binding.errorPlaceholder.isVisible = true }
                is SearchState.History -> { setAllGone(); if (st.items.isNotEmpty()) { binding.historyGroup.isVisible = true; historyAdapter.setData(st.items) } }
                is SearchState.Idle -> setAllGone()
            }
        }
    }

    private fun setAllGone() {
        binding.progressBar.isVisible = false
        binding.tracksRecyclerView.isVisible = false
        binding.emptyPlaceholder.isVisible = false
        binding.errorPlaceholder.isVisible = false
        binding.historyGroup.isVisible = false
    }

    private fun onTrackClicked(track: Track) {
        viewModel.onClickTrack(track)
        startActivity(Intent(this, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_TRACK, track))
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }
}