package com.juniperphoton.myersplash.extension

import android.app.Activity
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup

fun View.getActivity(): Activity? {
    var ctx = context
    while (ctx is ContextWrapper) {
        if (ctx is Activity) {
            return ctx
        }
        ctx = ctx.baseContext
    }
    return null
}

fun View.updateDimensions(width: Int, height: Int) {
    val lp = layoutParams ?: ViewGroup.LayoutParams(0, 0)
    lp.width = width
    lp.height = height
    layoutParams = lp
}