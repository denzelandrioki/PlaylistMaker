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
import com.practicum.playlistmaker.app.App

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val darkThemeSwitch = findViewById<SwitchMaterial>(R.id.darkThemeSwitch)
        val toolbar = findViewById<MaterialToolbar>(R.id.settingsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val app = applicationContext as App
        darkThemeSwitch.isChecked = app.darkTheme
        darkThemeSwitch.setOnCheckedChangeListener { _, isChecked -> app.switchTheme(isChecked) }

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
            val email = getString(R.string.support_email)
            val subject = getString(R.string.support_email_subject)
            val body = getString(R.string.support_email_body)
            val mailto = "mailto:$email?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(mailto))
            try { startActivity(emailIntent) }
            catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "Почтовый клиент не найден", Toast.LENGTH_SHORT).show()
            }
        }

        userAgreementButton.setOnClickListener {
            val url = getString(R.string.user_agreement_url)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
