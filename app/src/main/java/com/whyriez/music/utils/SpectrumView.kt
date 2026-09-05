package com.whyriez.music.utils

import android.R
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.hypot
import kotlin.random.Random

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
    private var animator: ValueAnimator? = null

    private var isSimulating = false

    fun updateVisualizer(fft: ByteArray) {
        if (fft.size < barCount * 2) return
        for (i in 0 until barCount) {
            val r = fft[2 * i].toFloat()
            val im = fft[2 * i + 1].toFloat()
            magnitudes[i] = hypot(r.toDouble(), im.toDouble()).toFloat()
        }
        invalidate()
    }

    fun startSimulation() {
        isSimulating = true
        startInternalAnimator()
    }

    fun stopSimulation() {
        isSimulating = false
        stopInternalAnimator()
    }

    private fun startInternalAnimator() {
        if (animator?.isRunning == true || visibility != VISIBLE) return
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 150
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                for (i in 0 until barCount) {
                    magnitudes[i] = Random.nextFloat() * 45f + 5f
                }
                invalidate()
            }
            start()
        }
    }

    private fun stopInternalAnimator() {
        animator?.cancel()
        animator = null
        for (i in 0 until barCount) {
            magnitudes[i] = 4f
        }
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isSimulating) {
            startInternalAnimator()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopInternalAnimator()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            if (isSimulating) startInternalAnimator()
        } else {
            stopInternalAnimator()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val totalBarRatio = barCount * 2 - 1
        val barWidth = width.toFloat() / totalBarRatio
        val maxHeight = height.toFloat()

        for (i in 0 until barCount) {
            val left = i * (barWidth * 2)
            val right = left + barWidth
            val barHeight = ((magnitudes[i] / 50f) * maxHeight).coerceIn(4f, maxHeight)
            val top = maxHeight - barHeight

            canvas.drawRoundRect(left, top, right, maxHeight, 6f, 6f, paint)
        }
    }
}