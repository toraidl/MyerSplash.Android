package com.juniperphoton.myersplash.extension

import com.juniperphoton.flipperlayout.FlipperLayout

fun FlipperLayout.updateIndex(index: Int) {
    if (displayIndex != index) {
        next(index)
    }
}

fun FlipperLayout.updateIndexWithoutAnimation(nextIndex: Int) {
    if (displayIndex != nextIndex) {
        next(nextIndex, animate = false)
    }
}