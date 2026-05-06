package com.practicum.playlistmaker.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.presentation.search.compose.SearchScreen
import com.practicum.playlistmaker.presentation.theme.PlaylistMakerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Контейнер для Compose UI поиска; навигация — Jetpack Navigation. */
class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            PlaylistMakerTheme {
                SearchScreen(
                    viewModel = viewModel,
                    onTrackClick = { track -> openPlayer(track) },
                )
            }
        }
    }

    private fun openPlayer(track: Track) {
        viewModel.onClickTrack(track)
        findNavController().navigate(
            R.id.action_searchFragment_to_playerFragment,
            Bundle().apply { putParcelable("track", track) },
        )
    }
}
