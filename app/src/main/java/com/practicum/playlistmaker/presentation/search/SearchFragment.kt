package com.practicum.playlistmaker.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.practicum.playlistmaker.databinding.FragmentSearchBinding
import com.practicum.playlistmaker.domain.entity.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModel()

    private lateinit var tracksAdapter: TracksAdapter
    private lateinit var historyAdapter: TracksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tracksAdapter = TracksAdapter(mutableListOf()) { onTrackClicked(it) }
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecyclerView.adapter = tracksAdapter

        historyAdapter = TracksAdapter(mutableListOf()) { onTrackClicked(it) }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
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

        viewModel.state.observe(viewLifecycleOwner) { st ->
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
        // Навигация будет через Navigation Component
        val navController = androidx.navigation.Navigation.findNavController(requireView())
        navController.navigate(
            com.practicum.playlistmaker.R.id.action_searchFragment_to_playerFragment,
            android.os.Bundle().apply {
                putParcelable("track", track)
            }
        )
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}