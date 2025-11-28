package com.practicum.playlistmaker.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import com.practicum.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.settingsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val darkThemeSwitch = findViewById<SwitchMaterial>(R.id.darkThemeSwitch)
        viewModel.darkTheme.observe(this) { dark -> darkThemeSwitch.isChecked = dark }
        darkThemeSwitch.setOnCheckedChangeListener { _, checked -> viewModel.setDarkTheme(checked) }

        val shareButton = findViewById<MaterialTextView>(R.id.share_app_button)
        val supportButton = findViewById<MaterialTextView>(R.id.support_button)
        val userAgreementButton = findViewById<MaterialTextView>(R.id.user_agreement_button)

        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
        }

        supportButton.setOnClickListener {
            val mailto = "mailto:${getString(R.string.support_email)}" +
                    "?subject=${Uri.encode(getString(R.string.support_email_subject))}" +
                    "&body=${Uri.encode(getString(R.string.support_email_body))}"
            try {
                startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(mailto)))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "Почтовый клиент не найден", Toast.LENGTH_SHORT).show()
            }
        }

        userAgreementButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.user_agreement_url))))
        }
    }
}