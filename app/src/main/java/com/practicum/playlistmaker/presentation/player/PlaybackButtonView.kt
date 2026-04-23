package com.practicum.playlistmaker.presentation.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.practicum.playlistmaker.R

/**
 * Кнопка воспроизведения: два состояния — «Играть» и «Пауза».
 * Иконки задаются в XML ([R.styleable.PlaybackButtonView]).
 * Событие нажатия — при [MotionEvent.ACTION_UP]; синхронизация с плеером — [setPlaybackPlaying].
 */
class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var playBitmap: Bitmap? = null
    private var pauseBitmap: Bitmap? = null

    /** true — трек играет, показываем иконку «Пауза». */
    private var playbackPlaying = false

    private val iconDst = RectF()
    private val hitRect = Rect()

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    private var onPlaybackToggleListener: (() -> Unit)? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.PlaybackButtonView, defStyleAttr, 0).apply {
            try {
                val playRes = getResourceId(R.styleable.PlaybackButtonView_playbackIconPlay, 0)
                val pauseRes = getResourceId(R.styleable.PlaybackButtonView_playbackIconPause, 0)
                if (playRes != 0) playBitmap = drawableToBitmap(playRes)
                if (pauseRes != 0) pauseBitmap = drawableToBitmap(pauseRes)
            } finally {
                recycle()
            }
        }
        isClickable = true
    }

    private fun drawableToBitmap(resId: Int): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, resId) ?: return null
        val w = drawable.intrinsicWidth.takeIf { it > 0 } ?: DEFAULT_ICON_PX
        val h = drawable.intrinsicHeight.takeIf { it > 0 } ?: DEFAULT_ICON_PX
        return drawable.toBitmap(w, h, Bitmap.Config.ARGB_8888)
    }

    fun setOnPlaybackToggleListener(listener: (() -> Unit)?) {
        onPlaybackToggleListener = listener
    }

    /** Синхронизация с ViewModel (например, завершение трека без нажатия). */
    fun setPlaybackPlaying(playing: Boolean) {
        if (playbackPlaying == playing) return
        playbackPlaying = playing
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        hitRect.set(0, 0, w, h)
        val bmp = playBitmap ?: pauseBitmap ?: return
        val bw = bmp.width.toFloat()
        val bh = bmp.height.toFloat()
        val scale = minOf(w / bw, h / bh)
        val tw = bw * scale
        val th = bh * scale
        val left = (w - tw) / 2f
        val top = (h - th) / 2f
        iconDst.set(left, top, left + tw, top + th)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = if (playbackPlaying) pauseBitmap else playBitmap
        if (bmp != null && !iconDst.isEmpty) {
            canvas.drawBitmap(bmp, null, iconDst, bitmapPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_CANCEL -> return true
            MotionEvent.ACTION_UP -> {
                if (hitRect.contains(event.x.toInt(), event.y.toInt())) {
                    // Состояние иконки задаёт только ViewModel через setPlaybackPlaying (в т.ч. PREPARING / ошибка).
                    onPlaybackToggleListener?.invoke()
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    companion object {
        private const val DEFAULT_ICON_PX = 64
    }
}
