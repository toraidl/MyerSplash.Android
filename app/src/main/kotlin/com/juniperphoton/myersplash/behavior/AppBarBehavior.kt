package com.juniperphoton.myersplash.behavior

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.juniperphoton.myersplash.utils.Pasteur

private const val TAG = "AppBarBehavior"

class AppBarBehavior(context: Context?, attrs: AttributeSet?
) : CoordinatorLayout.Behavior<View>(context, attrs) {
    override fun onApplyWindowInsets(coordinatorLayout: CoordinatorLayout,
                                     child: View,
                                     insets: WindowInsetsCompat): WindowInsetsCompat {
        // Add padding to the top
        child.setPadding(0, insets.systemWindowInsetTop, 0, 0)
        return insets
    }

    private var rect = IntArray(2)
    private var animator: ValueAnimator? = null

    private var fling = false

    private fun childVisibleOnWindow(child: View): Boolean {
        child.getLocationInWindow(rect)
        return child.bottom + child.translationY > 0
    }

    private inline fun getTypeString(type: Int): String {
        return when (type) {
            ViewCompat.TYPE_TOUCH -> "touch"
            else -> "fling"
        }
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout,
                                     child: View,
                                     directTargetChild: View,
                                     target: View,
                                     axes: Int,
                                     type: Int): Boolean {
        animator?.cancel()

        if (axes != ViewCompat.SCROLL_AXIS_VERTICAL) {
            return false
        }

        Pasteur.info(TAG, "======onStartNestedScroll: type: ${getTypeString(type)}")

        fling = type != ViewCompat.TYPE_TOUCH

        return true
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout,
                                   child: View,
                                   target: View,
                                   dx: Int,
                                   dy: Int,
                                   consumed: IntArray,
                                   type: Int) {
        var targetTop = child.translationY - dy
        if (targetTop < 0) {
            if (targetTop <= -child.height.toFloat()) {
                consumed[1] = dy - (-child.height - targetTop.toInt())
                targetTop = -child.height.toFloat()
            } else {
                consumed[1] = dy
            }

            child.translationY = targetTop
        } else {
            child.translationY = 0f
            consumed[1] = -(dy - targetTop.toInt())
        }

        //Pasteur.warn(TAG, "======onNestedPreScroll: scroll to $dy, consumed $consumed[1]")
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, type: Int) {
        if (!childVisibleOnWindow(child)) {
            return
        }

        Pasteur.info(TAG, "======onStopNestedScroll: type: ${getTypeString(type)}")

        if (type == ViewCompat.TYPE_TOUCH) {
            animateToReset(child)
        }
    }

    private fun animateToReset(child: View) {
        if (animator?.isStarted == true) {
            return
        }

        var start = child.translationY
        var end = when {
            start < -child.height / 2 -> {
                -child.height.toFloat()
            }
            else -> {
                0f
            }
        }

        if (start != end) {
            animator = ValueAnimator.ofFloat(start, end).apply {
                addUpdateListener {
                    child.translationY = it.animatedValue as Float
                }
                start()
            }
        }
    }
}

class ScrollingBehavior(context: Context?, attrs: AttributeSet?
) : CoordinatorLayout.Behavior<View>(context, attrs) {
    override fun onApplyWindowInsets(coordinatorLayout: CoordinatorLayout,
                                     child: View,
                                     insets: WindowInsetsCompat): WindowInsetsCompat {
        return insets
    }

    override fun onMeasureChild(parent: CoordinatorLayout,
                                child: View,
                                parentWidthMeasureSpec: Int,
                                widthUsed: Int,
                                parentHeightMeasureSpec: Int,
                                heightUsed: Int): Boolean {
        // Compensate the default fitSystemWindowInsets behavior
        child.setPadding(0, 0, 0, 0)
        return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed)
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        // We only depends on the view with behavior of AppBarBehavior
        return (dependency.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior is AppBarBehavior
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val offset = dependency.bottom + dependency.translationY
        child.translationY = offset
        return offset != 0f
    }
}