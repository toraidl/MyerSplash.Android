package com.juniperphoton.myersplash.extension

import com.juniperphoton.flipperlayout.FlipperLayout

fun FlipperLayout.updateIndex(index: Int) {
    if (displayIndex != index) {
        next(index)
    }
}

fun FlipperLayout.updateIndexWithoutAnimation(index: Int) {
    if (displayIndex != index) {
        next(index, animate = false)
    }
}