package com.juniperphoton.myersplash.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet

/**
 * A card view that round itself to half of its height on measured.
 */
class RoundCardView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (height > 0) {
            radius = height / 2f
        }
    }
}