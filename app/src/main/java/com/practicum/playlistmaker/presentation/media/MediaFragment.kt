package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.entity.Playlist
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.presentation.media.compose.MediaScreen
import com.practicum.playlistmaker.presentation.theme.PlaylistMakerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Контейнер медиатеки (Compose внутри; глобальная навигация без изменений). */
class MediaFragment : Fragment() {

    private val favoritesViewModel: FavoritesViewModel by viewModel()
    private val playlistsViewModel: PlaylistsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            PlaylistMakerTheme {
                MediaScreen(
                    favoritesViewModel = favoritesViewModel,
                    playlistsViewModel = playlistsViewModel,
                    onTrackClick = { openPlayer(it) },
                    onPlaylistClick = { openPlaylist(it) },
                    onNewPlaylistClick = { openCreatePlaylist() },
                )
            }
        }
    }

    private fun openPlayer(track: Track) {
        findNavController().navigate(
            R.id.action_mediaFragment_to_playerFragment,
            Bundle().apply { putParcelable("track", track) },
        )
    }

    private fun openPlaylist(playlist: Playlist) {
        findNavController().navigate(
            R.id.action_mediaFragment_to_playlistFragment,
            Bundle().apply { putLong("playlistId", playlist.id) },
        )
    }

    private fun openCreatePlaylist() {
        findNavController().navigate(R.id.action_mediaFragment_to_createPlaylistFragment)
    }
}
