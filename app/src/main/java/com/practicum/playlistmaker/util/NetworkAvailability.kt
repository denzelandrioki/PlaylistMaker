package com.practicum.playlistmaker.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Проверка доступности интернета (ту же логику можно вызывать перед сетевыми запросами).
 * [NET_CAPABILITY_VALIDATED] отсекает сеть без реального выхода в интернет (часто совпадает с ошибками HTTP/Glide).
 */
fun Context.isInternetAvailable(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
