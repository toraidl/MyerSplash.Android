package com.juniperphoton.myersplash.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.getStatusBarHeight
import com.juniperphoton.myersplash.extension.hasNavigationBar
import com.juniperphoton.myersplash.extension.updateDimensions

open class BaseActivity : AppCompatActivity() {
    private var systemUiConfigured = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 21) {
            window.statusBarColor = Color.TRANSPARENT
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onStart() {
        super.onStart()
        if (!systemUiConfigured) {
            systemUiConfigured = true
            onConfigNavigationBar(hasNavigationBar())
            onConfigStatusBar()
        }
    }

    open fun onConfigStatusBar() {
        findViewById<View>(R.id.status_bar_placeholder)?.updateDimensions(
                ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight())
    }

    open fun onConfigNavigationBar(hasNavigationBar: Boolean) = Unit
}
