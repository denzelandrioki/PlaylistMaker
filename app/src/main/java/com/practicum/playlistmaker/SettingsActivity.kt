package com.practicum.playlistmaker

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)


        // Найдем переключатель для темы
        val darkThemeSwitch = findViewById<SwitchMaterial>(R.id.darkThemeSwitch)


        // 1) Привязываем toolbar как ActionBar
        val toolbar = findViewById<MaterialToolbar>(R.id.settingsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        // 2) Обрабатываем клик на значок «назад»
        toolbar.setNavigationOnClickListener { finish() }

        // Применим сохраненное состояние или установим по умолчанию (например, дневной режим)
        // Можно сохранить выбор пользователя в SharedPreferences, но для примера установим дневной режим
        darkThemeSwitch.isChecked = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)

        // Обработка переключения темы
        darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Включаем ночной режим
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                // Включаем дневной режим
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            // При смене темы активность может быть перезапущена для применения изменений
            // Возможно, понадобится закрыть текущую активность и открыть её заново для полного обновления UI
        }


        // Находим кнопки по id
        val shareButton = findViewById<MaterialTextView>(R.id.share_app_button)
        val supportButton = findViewById<MaterialTextView>(R.id.support_button)
        val userAgreementButton = findViewById<MaterialTextView>(R.id.user_agreement_button)


        // Обработка нажатия кнопки "Поделиться приложением"
        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            // Создаем диалог для выбора приложения для шаринга
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
        }

        // Обработка нажатия кнопки "Написать в поддержку"
        supportButton.setOnClickListener{
            val email = getString(R.string.support_email)
            val subject = getString(R.string.support_email_subject)
            val body = getString(R.string.support_email_body)
            // Формируем URI для почтового клиента в формате mailto:
            val mailto = "mailto:$email" +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(body)
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse(mailto)
            try {
                startActivity(emailIntent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, "Почтовый клиент не найден", Toast.LENGTH_SHORT).show()
            }

        }


        // Обработка нажатия кнопки "Пользовательское соглашение"
        userAgreementButton.setOnClickListener {
            val url = getString(R.string.user_agreement_url)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }




    }
}