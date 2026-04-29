package com.practicum.playlistmaker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import android.content.pm.ServiceInfo
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.presentation.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
private const val PLAYBACK_ATTRIBUTION_TAG = "audioPlayback"

private const val TICK_MS = 300L

/**
 * Bound + foreground-сервис: воспроизведение превью, аудиофокус и уведомление при воспроизведении в фоне.
 */
class PlayerService : android.app.Service(), PlayerAudioControl {

    private val binder = PlayerBinder()

    /** Глобально проинициализированный Binder — возвращается из [onBind]. */
    inner class PlayerBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main.immediate)

    /**
     * Не инициализировать в полях класса: у [Service] к моменту конструктора [Context] ещё не
     * прикреплён, [createAttributionContext] падает с NPE (см. stack trace на API 30+).
     */
    private lateinit var playbackContext: Context

    private lateinit var audioManager: AudioManager

    private var audioFocusRequest: AudioFocusRequest? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val downloadExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "playlistmaker-preview-download").apply { priority = Process.THREAD_PRIORITY_BACKGROUND }
    }

    private val prepareSession = AtomicLong(0L)

    private var mp: MediaPlayer? = null
    private var cachedPreviewFile: File? = null

    private val mediaPlayerFactory: () -> MediaPlayer = { MediaPlayer() }

    /** Присваиваются в [onBind] из Intent (в т.ч. URL превью для соответствия данным экрана плеера). */
    private var boundPreviewUrl: String? = null

    private var artistNameForNotification: String = ""

    private var trackTitleForNotification: String = ""

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _progressMs = MutableStateFlow(0)
    override val progressMs: StateFlow<Int> = _progressMs.asStateFlow()

    private var progressJob: Job? = null

    private var onPausedByAudioFocus: (() -> Unit)? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (_playerState.value == PlayerState.PLAYING) {
                    runCatching { mp?.pause() }
                    _playerState.value = PlayerState.PAUSED
                    stopProgressTicker()
                    onPausedByAudioFocus?.invoke()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> Unit
        }
    }

    override fun onCreate() {
        super.onCreate()
        playbackContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            createAttributionContext(PLAYBACK_ATTRIBUTION_TAG)
        } else {
            this
        }
        audioManager = playbackContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        boundPreviewUrl = intent?.getStringExtra(EXTRA_PREVIEW_URL)
        artistNameForNotification = intent?.getStringExtra(EXTRA_ARTIST_NAME).orEmpty()
        trackTitleForNotification = intent?.getStringExtra(EXTRA_TRACK_NAME).orEmpty()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        hideForegroundNotification()
        stopPlaybackInternal()
        serviceJob.cancel()
        stopSelf()
        return false
    }

    override fun onDestroy() {
        stopPlaybackInternal()
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.player_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.player_notification_channel_description)
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }

    private fun mediaAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mediaAudioAttributes())
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioFocusRequest = req
            audioManager.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN,
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
                audioFocusRequest = null
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }

    override fun prepare(
        url: String,
        onPrepared: () -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        stopPlaybackInternal()
        if (url.isBlank()) {
            onError(IllegalArgumentException("previewUrl is blank"))
            return
        }

        _playerState.value = PlayerState.PREPARING
        val session = prepareSession.get()

        if (url.startsWith("content://")) {
            try {
                mp = mediaPlayerFactory().apply {
                    setAudioAttributes(mediaAudioAttributes())
                    setVolume(1f, 1f)
                    setDataSource(playbackContext, Uri.parse(url))
                    wireListeners(onPrepared, onComplete, onError)
                    prepareAsync()
                }
            } catch (t: Throwable) {
                _playerState.value = PlayerState.ERROR
                onError(t)
            }
            return
        }

        downloadExecutor.execute {
            val file = try {
                downloadPreviewToCache(url)
            } catch (t: Throwable) {
                mainHandler.post {
                    if (session != prepareSession.get()) return@post
                    _playerState.value = PlayerState.ERROR
                    onError(t)
                }
                return@execute
            }

            mainHandler.post {
                if (session != prepareSession.get()) {
                    file.delete()
                    return@post
                }
                try {
                    cachedPreviewFile?.delete()
                    cachedPreviewFile = file
                    mp = mediaPlayerFactory().apply {
                        setAudioAttributes(mediaAudioAttributes())
                        setVolume(1f, 1f)
                        setDataSource(file.absolutePath)
                        wireListeners(onPrepared, onComplete, onError)
                        prepareAsync()
                    }
                } catch (t: Throwable) {
                    _playerState.value = PlayerState.ERROR
                    file.delete()
                    cachedPreviewFile = null
                    onError(t)
                }
            }
        }
    }

    private fun MediaPlayer.wireListeners(
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        setOnPreparedListener {
            _playerState.value = PlayerState.PREPARED
            _progressMs.value = 0
            onPrepared()
        }
        setOnCompletionListener {
            _playerState.value = PlayerState.COMPLETED
            _progressMs.value = 0
            abandonAudioFocus()
            stopProgressTicker()
            hideForegroundNotification()
            onCompletion()
        }
        setOnErrorListener { _, _, _ ->
            _playerState.value = PlayerState.ERROR
            abandonAudioFocus()
            stopProgressTicker()
            hideForegroundNotification()
            onError(IllegalStateException("MediaPlayer error"))
            true
        }
    }

    private fun downloadPreviewToCache(sourceUrl: String): File {
        val connection = (URL(sourceUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 45_000
            instanceFollowRedirects = true
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 PlaylistMaker/1.0",
            )
        }
        try {
            connection.connect()
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("HTTP $code for preview URL")
            }
            val ext = when {
                ".m4a" in sourceUrl.lowercase() -> "m4a"
                ".mp3" in sourceUrl.lowercase() -> "mp3"
                ".aac" in sourceUrl.lowercase() -> "aac"
                else -> "bin"
            }
            val out = File.createTempFile("preview_", ".$ext", cacheDir)
            connection.inputStream.use { input ->
                FileOutputStream(out).use { output -> input.copyTo(output) }
            }
            if (out.length() == 0L) {
                out.delete()
                throw IOException("Empty preview download")
            }
            return out
        } finally {
            connection.disconnect()
        }
    }

    override fun play() {
        val player = mp ?: return
        when (_playerState.value) {
            PlayerState.PREPARED, PlayerState.PAUSED -> {
                requestAudioFocus()
                player.start()
                _playerState.value = PlayerState.PLAYING
                startProgressTicker()
            }
            PlayerState.COMPLETED -> {
                requestAudioFocus()
                player.seekTo(0)
                player.start()
                _playerState.value = PlayerState.PLAYING
                startProgressTicker()
            }
            PlayerState.PREPARING, PlayerState.IDLE, PlayerState.PLAYING, PlayerState.ERROR -> Unit
        }
    }

    override fun pause() {
        if (_playerState.value != PlayerState.PLAYING) return
        mp?.pause()
        _playerState.value = PlayerState.PAUSED
        abandonAudioFocus()
        stopProgressTicker()
        hideForegroundNotification()
    }

    override fun stop() {
        stopPlaybackInternal()
    }

    private fun stopPlaybackInternal() {
        prepareSession.incrementAndGet()
        abandonAudioFocus()
        mp?.release()
        mp = null
        _playerState.value = PlayerState.IDLE
        _progressMs.value = 0
        cachedPreviewFile?.delete()
        cachedPreviewFile = null
        stopProgressTicker()
    }

    override fun currentPositionMs(): Int = mp?.currentPosition ?: 0

    override fun setOnPausedByAudioFocusListener(listener: (() -> Unit)?) {
        onPausedByAudioFocus = listener
    }

    private fun startProgressTicker() {
        stopProgressTicker()
        progressJob = serviceScope.launch {
            while (isActive && _playerState.value == PlayerState.PLAYING) {
                _progressMs.value = mp?.currentPosition ?: 0
                delay(TICK_MS)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun showForegroundNotification() {
        if (_playerState.value != PlayerState.PLAYING) return
        val notification = buildNotification()
        try {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
        } catch (_: Exception) {
            // POST_NOTIFICATIONS или ограничения OEM — воспроизведение не прерываем
            @Suppress("DEPRECATION")
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun hideForegroundNotification() {
        try {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun buildNotification(): android.app.Notification {
        val openApp = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this,
            0,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = listOf(artistNameForNotification.trim(), trackTitleForNotification.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" - ")
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_legacy_square)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        const val EXTRA_PREVIEW_URL = "extra_preview_url"
        const val EXTRA_ARTIST_NAME = "extra_artist_name"
        const val EXTRA_TRACK_NAME = "extra_track_name"

        private const val CHANNEL_ID = "playlistmaker_player"
        private const val NOTIFICATION_ID = 71031
    }
}
