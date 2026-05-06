package com.practicum.playlistmaker.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.settings.compose.SettingsScreen
import com.practicum.playlistmaker.presentation.theme.PlaylistMakerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Контейнер экрана настроек на Compose; интенты остаются во Fragment. */
class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            PlaylistMakerTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onShareApp = { shareApp() },
                    onSupport = { openSupport() },
                    onUserAgreement = { openUserAgreement() },
                )
            }
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
    }

    private fun openSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.support_email_body))
        }
        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.support)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Почтовый клиент не найден", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUserAgreement() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.user_agreement_url))))
    }
}
