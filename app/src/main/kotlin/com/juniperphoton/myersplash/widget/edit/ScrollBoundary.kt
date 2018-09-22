package com.juniperphoton.myersplash.widget.edit

import android.widget.OverScroller

/**
 * Represents a scroll boundary used with [OverScroller].
 */
interface ScrollBoundary {
    val minX: Int
    val maxX: Int
    val minY: Int
    val maxY: Int
}