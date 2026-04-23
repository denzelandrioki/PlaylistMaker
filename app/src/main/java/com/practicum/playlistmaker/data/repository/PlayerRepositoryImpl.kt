package com.practicum.playlistmaker.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.practicum.playlistmaker.domain.entity.PlayerState
import com.practicum.playlistmaker.domain.repository.PlayerRepository
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/** Совпадает с `android:tag` у второго `<attribution>` в AndroidManifest. */
private const val PLAYBACK_ATTRIBUTION_TAG = "audioPlayback"

/**
 * Воспроизведение превью по URL через MediaPlayer.
 *
 * Для **http/https** превью сначала скачиваем файл в cache и вызываем [MediaPlayer.setDataSource] с путём —
 * так стабильнее на эмуляторе и не цепляемся за ContentResolver (как у `setDataSource(Context, Uri)` для https).
 *
 * [AudioAttributes], аудиофокус и [createAttributionContext] для [AudioManager] (API 30+).
 */
class PlayerRepositoryImpl(
    private val appContext: Context,
    private val mediaPlayerFactory: () -> MediaPlayer,
) : PlayerRepository {

    private var mp: MediaPlayer? = null
    private var state: PlayerState = PlayerState.IDLE

    private var onPausedByAudioFocus: (() -> Unit)? = null

    private val playbackContext: Context =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            appContext.createAttributionContext(PLAYBACK_ATTRIBUTION_TAG)
        } else {
            appContext
        }

    private val audioManager: AudioManager =
        playbackContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var audioFocusRequest: AudioFocusRequest? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val downloadExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "playlistmaker-preview-download")
    }

    /** Инкрементируется в [stop]; отменяет «устаревшие» коллбеки загрузки. */
    private val prepareSession = AtomicLong(0L)

    private var cachedPreviewFile: File? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (state == PlayerState.PLAYING) {
                    runCatching { mp?.pause() }
                    state = PlayerState.PAUSED
                    onPausedByAudioFocus?.invoke()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> Unit
        }
    }

    override fun setOnPausedByAudioFocusListener(listener: (() -> Unit)?) {
        onPausedByAudioFocus = listener
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
        url: String?,
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        stop()
        if (url.isNullOrBlank()) {
            onError(IllegalArgumentException("previewUrl is null"))
            return
        }

        state = PlayerState.PREPARING
        val session = prepareSession.get()

        if (url.startsWith("content://")) {
            try {
                mp = mediaPlayerFactory().apply {
                    setAudioAttributes(mediaAudioAttributes())
                    setVolume(1f, 1f)
                    setDataSource(playbackContext, Uri.parse(url))
                    wireListeners(onPrepared, onCompletion, onError)
                    prepareAsync()
                }
            } catch (t: Throwable) {
                state = PlayerState.ERROR
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
                    state = PlayerState.ERROR
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
                        wireListeners(onPrepared, onCompletion, onError)
                        prepareAsync()
                    }
                } catch (t: Throwable) {
                    state = PlayerState.ERROR
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
            state = PlayerState.PREPARED
            onPrepared()
        }
        setOnCompletionListener {
            state = PlayerState.COMPLETED
            abandonAudioFocus()
            onCompletion()
        }
        setOnErrorListener { _, _, _ ->
            state = PlayerState.ERROR
            abandonAudioFocus()
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
            val out = File.createTempFile("preview_", ".$ext", appContext.cacheDir)
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
        when (state) {
            PlayerState.PREPARED, PlayerState.PAUSED -> {
                requestAudioFocus()
                player.start()
                state = PlayerState.PLAYING
            }
            PlayerState.COMPLETED -> {
                requestAudioFocus()
                player.seekTo(0)
                player.start()
                state = PlayerState.PLAYING
            }
            PlayerState.PREPARING, PlayerState.IDLE, PlayerState.PLAYING, PlayerState.ERROR -> Unit
        }
    }

    override fun pause() {
        if (state != PlayerState.PLAYING) return
        mp?.pause()
        state = PlayerState.PAUSED
        abandonAudioFocus()
    }

    override fun stop() {
        prepareSession.incrementAndGet()
        abandonAudioFocus()
        mp?.release()
        mp = null
        state = PlayerState.IDLE
        cachedPreviewFile?.delete()
        cachedPreviewFile = null
    }

    override fun currentPositionMs(): Int = mp?.currentPosition ?: 0

    override fun state(): PlayerState = state
}
