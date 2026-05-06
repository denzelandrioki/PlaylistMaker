package com.practicum.playlistmaker.presentation.search.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.entity.Track
import com.practicum.playlistmaker.presentation.common.compose.TrackListItem
import com.practicum.playlistmaker.presentation.search.SearchState
import com.practicum.playlistmaker.presentation.search.SearchViewModel

/** Экран поиска: отображение [SearchState] и передача действий в [SearchViewModel]. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onTrackClick: (Track) -> Unit,
) {
    val state by viewModel.state.observeAsState(SearchState.History(emptyList()))
    val query by viewModel.searchQuery.observeAsState("")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.search),
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
            SearchField(
                query = query,
                onQueryChange = { viewModel.onQueryChanged(it) },
                onClear = { viewModel.onQueryChanged("") },
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                // Локальная копия — для умного приведения типа (observeAsState даёт свойство с геттером).
                val screenState = state
                when (screenState) {
                    SearchState.Idle -> Unit
                    SearchState.Loading -> LoadingContent()
                    is SearchState.Content -> TracksList(
                        tracks = screenState.items,
                        onTrackClick = onTrackClick,
                    )
                    SearchState.Empty -> EmptyPlaceholder()
                    SearchState.Error -> ErrorPlaceholder(onRetry = { viewModel.onRetry() })
                    is SearchState.History -> {
                        if (screenState.items.isNotEmpty()) {
                            HistoryContent(
                                tracks = screenState.items,
                                onClearHistory = { viewModel.onClearHistory() },
                                onTrackClick = onTrackClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Поле ввода: только ввод текста и вызов дебаунса через ViewModel. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val fieldBg = colorResource(R.color.searchField_light)
    val iconTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 8.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        cursorBrush = SolidColor(colorResource(R.color.primary_blue)),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboard?.hide()
                onQueryChange(query)
            },
        ),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(fieldBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search_16),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                    tint = iconTint,
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = iconTint,
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_clear_24),
                            contentDescription = stringResource(R.string.clear),
                            tint = iconTint,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 140.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(44.dp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun TracksList(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
            .padding(horizontal = 13.dp),
    ) {
        items(tracks, key = { it.trackId }) { track ->
            TrackListItem(track = track, onClick = { onTrackClick(track) })
        }
    }
}

@Composable
private fun HistoryContent(
    tracks: List<Track>,
    onClearHistory: () -> Unit,
    onTrackClick: (Track) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
            .padding(horizontal = 13.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
            )
        }
        items(tracks, key = { it.trackId }) { track ->
            TrackListItem(track = track, onClick = { onTrackClick(track) })
        }
        item {
            Button(
                onClick = onClearHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.history_btn_bg),
                    contentColor = colorResource(R.color.history_btn_text),
                ),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text(
                    text = stringResource(R.string.clear_history),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun EmptyPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 102.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_no_results),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = stringResource(R.string.no_tracks_found),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun ErrorPlaceholder(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 102.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_server_error),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = stringResource(R.string.server_error),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A1B22),
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.retry),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
