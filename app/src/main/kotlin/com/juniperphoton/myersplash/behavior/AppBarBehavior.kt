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

private inline fun View.minScrollingY(): Int {
    return -height
}

private inline fun View.maxScrollingY(): Int {
    return 0
}

/**
 * A behavior like the AppBarLayout's behavior in support library.
 * But we make the ui extend to the beneath area of system bar.
 *
 * The scrolling view should attach [ScrollingBehavior].
 */
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

    private fun childVisibleOnWindow(child: View): Boolean {
        child.getLocationInWindow(rect)
        return child.bottom + child.translationY > 0
    }

    /**
     * Helper inline method to convert touch type to string.
     */
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

        // We only care about vertical scrolling.
        if (axes != ViewCompat.SCROLL_AXIS_VERTICAL) {
            return false
        }

        Pasteur.info(TAG, "======onStartNestedScroll: type: ${getTypeString(type)}")
        return true
    }

    private fun minScrollableY(child: View): Float {
        return -child.height.toFloat()
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout,
                                   child: View,
                                   target: View,
                                   dx: Int,
                                   dy: Int,
                                   consumed: IntArray,
                                   type: Int) {
        var targetTop = child.translationY - dy

        // Contents are scrolling up.
        if (targetTop < 0) {
            // If the targetTop of AppBar exceeds the min scrolling y,
            // we only consume some of the y and restrict the AppBar's y to min scrolling y.
            if (targetTop <= child.minScrollingY()) {
                consumed[1] = dy - (child.minScrollingY() - targetTop.toInt())
                targetTop = child.minScrollingY().toFloat()
            } else {
                consumed[1] = dy
            }

            child.translationY = targetTop
        } else {
            // Contents are scrolling down but exceeds the max scrolling y.
            child.translationY = child.maxScrollingY().toFloat()
            consumed[1] = targetTop.toInt() - dy
        }

        //Pasteur.warn(TAG, "======onNestedPreScroll: scroll to $dy, consumed $consumed[1]")
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, type: Int) {
        if (!childVisibleOnWindow(child)) {
            return
        }

        Pasteur.info(TAG, "======onStopNestedScroll: type: ${getTypeString(type)}")

        // If this scroll is initialed by a touch, we snap the AppBar.
        if (type == ViewCompat.TYPE_TOUCH) {
            animateToReset(child)
        }
    }

    private fun animateToReset(child: View) {
        if (animator?.isStarted == true) {
            return
        }

        val start = child.translationY
        val end = when {
            start < -child.height / 2 -> {
                child.minScrollingY().toFloat()
            }
            else -> {
                child.maxScrollingY().toFloat()
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
        // This scrolling view should always follow the dependency's butt.
        child.translationY = offset
        return offset != 0f
    }
}