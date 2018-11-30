package com.juniperphoton.myersplash.widget

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView

/**
 * A card view that round itself to half of its height on measured.
 */
class RoundCardView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val targetRadius = (height / 2).toFloat()
        if (height > 0 && radius != targetRadius) {
            radius = targetRadius
        }
    }
}