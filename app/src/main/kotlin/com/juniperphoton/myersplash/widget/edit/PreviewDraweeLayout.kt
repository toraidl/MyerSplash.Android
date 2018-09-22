package com.juniperphoton.myersplash.widget.edit

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

/**
 * A frame layout that handles content scaling and translation gesture.
 * It must have at least one child.
 */
class PreviewDraweeLayout(context: Context,
                          attributeSet: AttributeSet?
) : FrameLayout(context, attributeSet) {
    private val contentView: View
        get() {
            return getChildAt(0)
        }

    private var scrollBoundary = EditScrollBoundary(context)
    private var attacher = ImagePreviewAttacher(context, scrollBoundary)

    fun updateContentScale(rect: RectF) {
        val width = rect.width()
        val height = rect.height()

        val screenWidth = contentView.width
        val screenHeight = contentView.height

        val scaleX = screenWidth.toFloat() / width
        val scaleY = screenHeight.toFloat() / height

        scrollBoundary.finalScale = Math.max(scaleX, scaleY)
        contentView.scaleX = scrollBoundary.finalScale
        contentView.scaleY = scrollBoundary.finalScale
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        attacher.anchorTo(contentView)
        attacher.dispatchInvalidateEventBy(this)
    }

    override fun computeScroll() {
        attacher.computeScroll()
    }
}