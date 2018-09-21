package com.juniperphoton.myersplash.widget.edit

import android.content.Context
import com.juniperphoton.myersplash.extension.getScreenWidth

class EditScrollBoundary(private val context: Context) : ScrollBoundary {
    /**
     * Indicate whether we should fix position up when action up event occurs
     */
    private val maxTranslationAbsX
        get() = (actualBoundWidth - context.getScreenWidth()) / 2f

    /**
     * Scale factor for image scaling.
     */
    var finalScale = 1f

    /**
     * Width of image after apply scaling
     */
    private val actualBoundWidth
        get() = context.getScreenWidth() * finalScale

    override val minX: Int
        get() = -maxTranslationAbsX.toInt()

    override val maxX: Int
        get() = maxTranslationAbsX.toInt()

    override val minY: Int
        get() = 0

    override val maxY: Int
        get() = 0
}