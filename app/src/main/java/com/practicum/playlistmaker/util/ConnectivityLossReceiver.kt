package com.practicum.playlistmaker.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager

/**
 * Реакция на [ConnectivityManager.CONNECTIVITY_ACTION]: при переходе «интернет был → нет» вызывается [onLost].
 */
class ConnectivityLossReceiver(
    private val onLost: () -> Unit,
) : BroadcastReceiver() {

    private var hadInternet: Boolean = true

    fun captureInitialState(context: Context) {
        hadInternet = context.isInternetAvailable()
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ConnectivityManager.CONNECTIVITY_ACTION) return
        val now = context.isInternetAvailable()
        if (hadInternet && !now) {
            onLost()
        }
        hadInternet = now
    }

    companion object {
        @Suppress("DEPRECATION")
        fun intentFilter(): IntentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }
}
