package com.practicum.playlistmaker.presentation.settings.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.settings.SettingsViewModel

/**
 * Экран настроек: переключение темы во ViewModel, остальные действия через callbacks во Fragment
 * (интенты остаются в Android-слое, как в прежней реализации).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onShareApp: () -> Unit,
    onSupport: () -> Unit,
    onUserAgreement: () -> Unit,
) {
    val darkTheme by viewModel.darkTheme.observeAsState(false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.dark_theme),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = darkTheme,
                    onCheckedChange = { viewModel.setDarkTheme(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorResource(R.color.switch_thumb_on),
                        checkedTrackColor = colorResource(R.color.switch_track_on),
                        uncheckedThumbColor = colorResource(R.color.switch_thumb_off),
                        uncheckedTrackColor = colorResource(R.color.switch_track_off),
                    ),
                )
            }
            SettingsItem(
                title = stringResource(R.string.share_app),
                endIconDrawableRes = R.drawable.ic_share,
                onClick = onShareApp,
            )
            SettingsItem(
                title = stringResource(R.string.support),
                endIconDrawableRes = R.drawable.ic_support,
                onClick = onSupport,
            )
            SettingsItem(
                title = stringResource(R.string.user_agreement),
                endIconDrawableRes = R.drawable.ic_arrow_right,
                onClick = onUserAgreement,
            )
        }
    }
}
