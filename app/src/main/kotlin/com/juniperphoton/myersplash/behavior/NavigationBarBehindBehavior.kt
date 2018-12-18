package com.juniperphoton.myersplash.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat

class NavigationBarBehindBehavior(context: Context,
                                  attributes: AttributeSet?
) : CoordinatorLayout.Behavior<View>(context, attributes) {
    override fun onApplyWindowInsets(coordinatorLayout: CoordinatorLayout,
                                     child: View,
                                     insets: WindowInsetsCompat): WindowInsetsCompat {
        val lp = child.layoutParams as CoordinatorLayout.LayoutParams
        lp.topMargin = insets.systemWindowInsetTop
        child.setPadding(0, 0, 0, insets.systemWindowInsetBottom)
        child.layoutParams = lp
        return insets
    }
}