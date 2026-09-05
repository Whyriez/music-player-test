package com.whyriez.music.utils

import android.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.hypot

class SpectrumView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val barCount = 5
    private val magnitudes = FloatArray(barCount)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1DB954")
        style = Paint.Style.FILL
    }

    fun updateVisualizer(fft: ByteArray) {
        for (i in 0 until barCount) {
            val r = fft[2 * i].toFloat()
            val im = fft[2 * i + 1].toFloat()
            magnitudes[i] = hypot(r.toDouble(), im.toDouble()).toFloat()
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val barWidth = width.toFloat() / (barCount * 2 - 1)
        val maxHeight = height.toFloat()

        for (i in 0 until barCount) {
            val left = i * (barWidth * 2)
            val right = left + barWidth
            val barHeight = ((magnitudes[i] / 50f) * maxHeight).coerceIn(4f, maxHeight)
            val top = maxHeight - barHeight

            canvas.drawRoundRect(left, top, right, maxHeight, 8f, 8f, paint)
        }
    }
}