package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val adapter = TracksAdapter { track -> openPlayer(track) }

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
        binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.favoritesRecyclerView.adapter = adapter
        viewModel.list.observe(viewLifecycleOwner) { tracks ->
            if (tracks.isEmpty()) {
                binding.favoritesRecyclerView.visibility = View.GONE
                binding.emptyIcon.visibility = View.VISIBLE
                binding.emptyText.visibility = View.VISIBLE
            } else {
                binding.emptyIcon.visibility = View.GONE
                binding.emptyText.visibility = View.GONE
                binding.favoritesRecyclerView.visibility = View.VISIBLE
                adapter.setData(tracks)
            }
        }
    }

    private fun openPlayer(track: Track) {
        val navController = view?.let { Navigation.findNavController(it) }
            ?: (parentFragment?.view?.let { Navigation.findNavController(it) })
        navController?.navigate(
            R.id.action_mediaFragment_to_playerFragment,
            Bundle().apply { putParcelable("track", track) }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
