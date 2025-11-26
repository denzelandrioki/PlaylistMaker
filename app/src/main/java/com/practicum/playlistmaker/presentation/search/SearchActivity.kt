package com.practicum.playlistmaker.presentation.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.presentation.player.PlayerActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val vm: SearchViewModel by viewModels { Creator.provideSearchViewModelFactory(this) }

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

        // adapters
        tracksAdapter = TracksAdapter(mutableListOf()) { onTrackClicked(it) }
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tracksRecyclerView.adapter = tracksAdapter

        historyAdapter = TracksAdapter(mutableListOf()) { onTrackClicked(it) }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

        // ui listeners
        binding.clearButton.setOnClickListener { binding.searchEditText.text?.clear() }
        binding.clearHistoryButton.setOnClickListener { vm.onClearHistory() }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                vm.onQueryChanged(binding.searchEditText.text.toString())
                true
            } else false
        }

        binding.searchEditText.addTextChangedListener(SimpleTextWatcher { text ->
            binding.clearButton.isVisible = text.isNotEmpty()
            vm.onQueryChanged(text)
        })

        binding.retryButton.setOnClickListener { vm.onRetry() }

        // observe state
        vm.state.observe(this) { st ->
            when (st) {
                is SearchState.Loading -> {
                    setAllGone(); binding.progressBar.visibility = View.VISIBLE
                }
                is SearchState.Content -> {
                    setAllGone()
                    binding.tracksRecyclerView.visibility = View.VISIBLE
                    tracksAdapter.setData(st.items)
                }
                is SearchState.Empty -> {
                    setAllGone(); binding.emptyPlaceholder.visibility = View.VISIBLE
                }
                is SearchState.Error -> {
                    setAllGone(); binding.errorPlaceholder.visibility = View.VISIBLE
                }
                is SearchState.History -> {
                    setAllGone()
                    if (st.items.isNotEmpty()) {
                        binding.historyGroup.visibility = View.VISIBLE
                        historyAdapter.setData(st.items)
                    }
                }
                is SearchState.Idle -> setAllGone()
            }
        }
    }

    private fun setAllGone() {
        binding.progressBar.visibility = View.GONE
        binding.tracksRecyclerView.visibility = View.GONE
        binding.emptyPlaceholder.visibility = View.GONE
        binding.errorPlaceholder.visibility = View.GONE
        binding.historyGroup.visibility = View.GONE
    }

    private fun onTrackClicked(track: Track) {
        vm.onClickTrack(track)
        startActivity(Intent(this, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_TRACK, track))
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }
}
