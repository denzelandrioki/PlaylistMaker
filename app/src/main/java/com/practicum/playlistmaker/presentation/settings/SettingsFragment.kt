package com.practicum.playlistmaker.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentSettingsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val darkThemeSwitch = binding.darkThemeSwitch
        viewModel.darkTheme.observe(viewLifecycleOwner) { dark -> 
            darkThemeSwitch.isChecked = dark 
        }
        darkThemeSwitch.setOnCheckedChangeListener { _, checked -> 
            viewModel.setDarkTheme(checked) 
        }

        binding.shareAppButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
        }

        binding.supportButton.setOnClickListener {
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

        binding.userAgreementButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.user_agreement_url))))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}