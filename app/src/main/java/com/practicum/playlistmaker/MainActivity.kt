package com.practicum.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<LinearLayout>(R.id.search)

        val mediaButton = findViewById<LinearLayout>(R.id.media)

        val settingButton = findViewById<LinearLayout>(R.id.setting)

        searchButton.setOnClickListener{
            val searchIntent = Intent(this, SearchActivity::class.java)
            startActivity(searchIntent)
        }
        mediaButton.setOnClickListener{
            val mediaIntent = Intent(this, MediaActivity::class.java)
            startActivity(mediaIntent)
        }

        settingButton.setOnClickListener{
            val settingIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingIntent)
        }



    }
}