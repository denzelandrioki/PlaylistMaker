package com.practicum.playlistmaker.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.app.App
import com.practicum.playlistmaker.creator.Creator

class SettingsActivity : AppCompatActivity() {

    private val vm: SettingsViewModel by viewModels { Creator.provideSettingsViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.settingsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val darkSwitch = findViewById<SwitchMaterial>(R.id.darkThemeSwitch)

        vm.dark.observe(this) { dark ->
            // применяем тему в приложении
            (application as App).switchTheme(dark)
            if (darkSwitch.isChecked != dark) darkSwitch.isChecked = dark
        }
        darkSwitch.setOnCheckedChangeListener { _, isChecked -> vm.toggle(isChecked) }

        // share
        findViewById<MaterialTextView>(R.id.share_app_button).setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share_app)))
        }

        // terms
        findViewById<MaterialTextView>(R.id.user_agreement_button).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.user_agreement_url))))
        }

        // support
        findViewById<MaterialTextView>(R.id.support_button).setOnClickListener {
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
    }
}
