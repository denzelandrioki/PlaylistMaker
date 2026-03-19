package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModel()
    private val adapter = PlaylistsAdapter(onPlaylistClick = { playlist ->
        parentFragment?.view?.let { view ->
            Navigation.findNavController(view).navigate(
                R.id.action_mediaFragment_to_playlistFragment,
                Bundle().apply { putLong("playlistId", playlist.id) },
            )
        }
    })

    companion object {
        fun newInstance() = PlaylistsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.newPlaylistButton.setOnClickListener {
            val navController = parentFragment?.view?.let { Navigation.findNavController(it) }
                ?: return@setOnClickListener
            navController.navigate(R.id.action_mediaFragment_to_createPlaylistFragment)
        }

        binding.playlistsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsRecyclerView.adapter = adapter

        viewModel.playlists.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.playlistsRecyclerView.visibility = View.GONE
                binding.emptyIcon.visibility = View.VISIBLE
                binding.emptyText.visibility = View.VISIBLE
            } else {
                binding.emptyIcon.visibility = View.GONE
                binding.emptyText.visibility = View.GONE
                binding.playlistsRecyclerView.visibility = View.VISIBLE
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

