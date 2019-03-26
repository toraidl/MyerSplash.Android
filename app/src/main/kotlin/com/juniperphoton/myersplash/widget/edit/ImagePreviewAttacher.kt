package com.juniperphoton.myersplash.widget.edit

import android.content.Context
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import com.juniperphoton.myersplash.utils.Pasteur

/**
 * Helper class to scroll the [contentView] and perform fling and overscroll.
 * See [anchorTo], [dispatchInvalidateEventBy] and [computeScroll] for details.
 */
class ImagePreviewAttacher(context: Context,
                           private val scrollBoundary: ScrollBoundary) : View.OnTouchListener {
    companion object {
        private const val TAG = "ImagePreviewAttacher"
        private const val OVER_SCROLL_X_PX = 200
        private const val RESISTANT_FACTOR = 0.3f
    }

    private lateinit var contentView: View
    private lateinit var touchEventHandler: View

    /**
     * The last translation x when action up
     */
    private var downTranslationX = 0f

    /**
     * Raw x when action down
     */
    private var actionDownX = 0f

    private var minimumFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity

    private var velocityTracker = VelocityTracker.obtain()
    private val scroller = OverScroller(context)

    /**
     * Attach to the content [view] that will update its translation X during scrolling.
     */
    fun anchorTo(view: View) {
        contentView = view
        contentView.setOnTouchListener(this)
    }

    /**
     * Dispatch invalidation event to the [view]. The [view] normally is
     * the parent of that in [anchorTo].
     */
    fun dispatchInvalidateEventBy(view: View) {
        touchEventHandler = view
    }

    /**
     * Compute scroll. Should be called on the view of [dispatchInvalidateEventBy]'s
     * computeScroll() method.
     */
    fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            val currX = scroller.currX.toFloat()
            contentView.translationX = currX
            ViewCompat.postInvalidateOnAnimation(touchEventHandler)
        }
    }

    private fun onStopAction() {
        actionDownX = 0f
        velocityTracker.computeCurrentVelocity(1000)
        val xVelocity = velocityTracker.xVelocity
        velocityTracker.recycle()
        velocityTracker = null

        Pasteur.i(TAG, "xVelocity: $xVelocity")

        if (Math.abs(xVelocity) < minimumFlingVelocity) {
            springBack()
        } else {
            fling(xVelocity)
        }
    }

    private fun springBack() {
        Pasteur.info(TAG, "spring back")
        scroller.springBack(
                contentView.translationX.toInt(), 0,
                scrollBoundary.minX, scrollBoundary.maxX,
                0, 0)
        ViewCompat.postInvalidateOnAnimation(touchEventHandler)
    }

    private fun fling(xVelocity: Float) {
        Pasteur.info(TAG, "fling: $xVelocity")
        scroller.forceFinished(true)
        scroller.fling(
                contentView.translationX.toInt(), 0,
                xVelocity.toInt(), 0,
                scrollBoundary.minX, scrollBoundary.maxX,
                0, 0,
                OVER_SCROLL_X_PX, 0)
        ViewCompat.postInvalidateOnAnimation(touchEventHandler)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!scroller.isFinished) {
                    scroller.forceFinished(true)
                    springBack()
                    return false
                }

                downTranslationX = contentView.translationX

                actionDownX = event.rawX
                velocityTracker?.clear()
                velocityTracker = VelocityTracker.obtain()
                velocityTracker.addMovement(event)
                scroller.abortAnimation()
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.rawX
                var dx = x - actionDownX
                if (contentView.translationX.toInt() !in scrollBoundary.minX..scrollBoundary.maxX) {
                    dx *= RESISTANT_FACTOR
                }
                contentView.translationX = dx + downTranslationX
                velocityTracker.addMovement(event)
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                onStopAction()
            }
        }
        return true
    }
}