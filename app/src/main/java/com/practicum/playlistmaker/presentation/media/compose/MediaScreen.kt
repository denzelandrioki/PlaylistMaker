package com.practicum.playlistmaker.presentation.media.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.entity.Playlist
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.presentation.common.compose.TrackListItem
import com.practicum.playlistmaker.presentation.media.FavoritesViewModel
import com.practicum.playlistmaker.presentation.media.PlaylistsViewModel
import kotlinx.coroutines.launch

/**
 * Медиатека: вкладки «Избранное» и «Плейлисты» через HorizontalPager без вложенных фрагментов.
 * Навигация остаётся на уровне [com.practicum.playlistmaker.presentation.media.MediaFragment].
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MediaScreen(
    favoritesViewModel: FavoritesViewModel,
    playlistsViewModel: PlaylistsViewModel,
    onTrackClick: (Track) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onNewPlaylistClick: () -> Unit,
) {
    val tabTitles = listOf(
        stringResource(R.string.favorite_tracks),
        stringResource(R.string.playlists),
    )
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.library),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> FavoritesTabContent(
                        viewModel = favoritesViewModel,
                        onTrackClick = onTrackClick,
                    )
                    else -> PlaylistsTabContent(
                        viewModel = playlistsViewModel,
                        onPlaylistClick = onPlaylistClick,
                        onNewPlaylistClick = onNewPlaylistClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesTabContent(
    viewModel: FavoritesViewModel,
    onTrackClick: (Track) -> Unit,
) {
    val tracks by viewModel.list.observeAsState(emptyList())
    if (tracks.isEmpty()) {
        MediaEmptyState(
            icon = R.drawable.ic_nomedia_light,
            message = stringResource(R.string.empty_media_library),
            topPadding = 106.dp,
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tracks, key = { it.trackId }) { track ->
                TrackListItem(
                    track = track,
                    onClick = { onTrackClick(track) },
                )
            }
        }
    }
}

@Composable
private fun PlaylistsTabContent(
    viewModel: PlaylistsViewModel,
    onPlaylistClick: (Playlist) -> Unit,
    onNewPlaylistClick: () -> Unit,
) {
    val playlists by viewModel.playlists.observeAsState(emptyList())
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = onNewPlaylistClick,
            modifier = Modifier
                .padding(top = 24.dp)
                .height(36.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.new_playlist_btn_bg),
                contentColor = colorResource(R.color.new_playlist_btn_text),
            ),
        ) {
            Text(
                text = stringResource(R.string.new_playlist),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        if (playlists.isEmpty()) {
            MediaEmptyState(
                icon = R.drawable.ic_nomedia_light,
                message = stringResource(R.string.no_playlists_message),
                topPadding = 44.dp,
            )
        } else {
            // Сетка 2 колонки, как во View-версии; LazyColumn здесь не подходит по макету.
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistGridItem(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistGridItem(
    playlist: Playlist,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val countText = pluralStringResource(
        R.plurals.tracks_count,
        playlist.trackCount,
        playlist.trackCount,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(playlist.coverUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder),
            )
        }
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = countText,
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(R.color.YP_Text_Gray),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun MediaEmptyState(
    icon: Int,
    message: String,
    topPadding: Dp,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
