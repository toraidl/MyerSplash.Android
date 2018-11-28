package com.juniperphoton.myersplash.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout

abstract class ScrollDismissBehavior<T : View>(context: Context?, attrs: AttributeSet?
) : CoordinatorLayout.Behavior<View>(context, attrs) {
    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return (dependency.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior is AppBarBehavior
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout,
                                        child: View,
                                        dependency: View): Boolean {
        onScrollStatusChanged(dependency.translationY == 0f, child as T)
        return false
    }

    protected open fun onScrollStatusChanged(show: Boolean, view: T) = Unit
}