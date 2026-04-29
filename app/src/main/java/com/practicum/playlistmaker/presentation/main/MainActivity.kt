package com.practicum.playlistmaker.presentation.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityMainBinding
import com.practicum.playlistmaker.presentation.common.ConnectivityLossToastHelper

/**
 * Единственная Activity: контейнер для Navigation Component и нижней навигации.
 * Граф навигации задаётся в nav_graph.xml, стартовый destination — mediaFragment (Медиатека).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val connectivityLossToastHelper by lazy {
        ConnectivityLossToastHelper(this) { isOnSearchOrPlayerDestination() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)

        // На экране плеера, создания и редактирования плейлиста нижняя панель скрывается
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideBottomNav = destination.id == R.id.playerFragment ||
                destination.id == R.id.createPlaylistFragment ||
                destination.id == R.id.editPlaylistFragment ||
                destination.id == R.id.playlistFragment
            binding.bottomNavigationView.visibility = if (hideBottomNav) android.view.View.GONE else android.view.View.VISIBLE
            binding.bottomNavDivider.visibility = if (hideBottomNav) android.view.View.GONE else android.view.View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        connectivityLossToastHelper.onStart()
    }

    override fun onStop() {
        connectivityLossToastHelper.onStop()
        super.onStop()
    }

    private fun isOnSearchOrPlayerDestination(): Boolean {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: return false
        return when (navHost.navController.currentDestination?.id) {
            R.id.searchFragment, R.id.playerFragment -> true
            else -> false
        }
    }
}
