package com.practicum.playlistmaker.presentation.common

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.util.ConnectivityLossReceiver

/**
 * Динамическая регистрация [ConnectivityLossReceiver] в паре [AppCompatActivity.onStart] /
 * [AppCompatActivity.onStop]: при свёрнутом приложении вызывается [onStop], Toast не нужен.
 *
 * Важно не использовать [AppCompatActivity.onPause]: при открытой шторке уведомлений Activity
 * часто уходит в paused и приёмник снимается до [ConnectivityManager.CONNECTIVITY_ACTION].
 */
class ConnectivityLossToastHelper(
    private val activity: AppCompatActivity,
    private val shouldNotify: () -> Boolean,
) {
    private var receiver: ConnectivityLossReceiver? = null

    fun onStart() {
        if (activity.isFinishing) return
        val rec = ConnectivityLossReceiver {
            if (activity.isFinishing || !shouldNotify()) return@ConnectivityLossReceiver
            Toast.makeText(
                activity,
                activity.getString(R.string.no_internet_toast),
                Toast.LENGTH_SHORT,
            ).show()
        }
        rec.captureInitialState(activity)
        receiver = rec
        val filter = ConnectivityLossReceiver.intentFilter()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(rec, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            activity.registerReceiver(rec, filter)
        }
    }

    fun onStop() {
        val rec = receiver ?: return
        runCatching { activity.unregisterReceiver(rec) }
        receiver = null
    }
}
