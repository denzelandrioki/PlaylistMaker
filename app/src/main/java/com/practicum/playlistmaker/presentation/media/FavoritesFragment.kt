package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentFavoritesBinding
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.presentation.search.TracksAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModel()
    private lateinit var adapter: TracksAdapter

    companion object {
        fun newInstance() = FavoritesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TracksAdapter(mutableListOf()) { track -> onTrackClicked(track) }
        binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.favoritesRecyclerView.adapter = adapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoritesState.Empty -> {
                    binding.emptyStateGroup.isVisible = true
                    binding.favoritesRecyclerView.isVisible = false
                }
                is FavoritesState.Content -> {
                    binding.emptyStateGroup.isVisible = false
                    binding.favoritesRecyclerView.isVisible = true
                    adapter.setData(state.tracks)
                }
            }
        }
    }

    private fun onTrackClicked(track: Track) {
        val navController = Navigation.findNavController(requireView())
        navController.navigate(
            R.id.action_mediaFragment_to_playerFragment,
            Bundle().apply { putParcelable("track", track) }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
