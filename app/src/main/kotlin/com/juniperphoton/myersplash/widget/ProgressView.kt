package com.juniperphoton.myersplash.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.use

@Suppress("unused")
class ProgressView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    companion object {
        private const val ANIMATION_DURATION_MS = 300L
    }

    private val paint = Paint()

    var color: Int = 0
    var progress: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CustomProgressView, 0, 0).use {
            color = getInt(R.styleable.CustomProgressView_background_color, Color.WHITE)
            progress = getInt(R.styleable.CustomProgressView_custom_progress, 0)
        }
    }

    fun animateProgressTo(progress: Int) {
        val animator = ValueAnimator.ofInt(this.progress, progress)
        animator.duration = ANIMATION_DURATION_MS
        animator.addUpdateListener { valueAnimator -> this.progress = (valueAnimator.animatedValue as Int) }
        animator.start()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        paint.color = color

        val width = width
        val progressWidth = (width * (progress / 100.0)).toInt()

        canvas.drawRect(0f, 0f, progressWidth.toFloat(), height.toFloat(), paint)
    }
}
