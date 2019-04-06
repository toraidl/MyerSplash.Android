package com.juniperphoton.myersplash.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.getStatusBarHeight
import com.juniperphoton.myersplash.extension.updateDimensions
import com.juniperphoton.myersplash.utils.Pasteur

abstract class BaseActivity : AppCompatActivity(), View.OnApplyWindowInsetsListener, View.OnClickListener {
    companion object {
        private const val TAG = "BaseActivity"
    }

    private var systemUiConfigured = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            val enter = resources.getIdentifier("android:activity_open_enter", "anim", null)
            val exit = resources.getIdentifier("android:activity_open_exit", "anim", null)
            if (enter != 0 && exit != 0) {
                overridePendingTransition(enter, exit)
            }
        }

        window.statusBarColor = Color.TRANSPARENT
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            val enter = resources.getIdentifier("android:activity_close_enter", "anim", null)
            val exit = resources.getIdentifier("android:activity_close_exit", "anim", null)
            if (enter != 0 && exit != 0) {
                overridePendingTransition(enter, exit)
            }
        }
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        super.startActivityForResult(intent, requestCode, options)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            val enter = resources.getIdentifier("android:activity_open_enter", "anim", null)
            val exit = resources.getIdentifier("android:activity_open_exit", "anim", null)
            if (enter != 0 && exit != 0) {
                overridePendingTransition(enter, exit)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val content = findViewById<View>(android.R.id.content)
        content.setOnApplyWindowInsetsListener(this)

        if (!systemUiConfigured) {
            systemUiConfigured = true
            onConfigStatusBar()
        }
    }

    open fun onConfigStatusBar() {
        findViewById<View>(R.id.status_bar_placeholder)?.updateDimensions(
                ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight())
    }

    open fun onApplySystemInsets(top: Int, bottom: Int) = Unit

    override fun onClick(v: View) {
        onClickView(v)
    }

    open fun onClickView(v: View) = Unit

    override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
        Pasteur.info(TAG, "height: ${insets.systemWindowInsetBottom}")
        onApplySystemInsets(insets.systemWindowInsetTop, insets.systemWindowInsetBottom)
        return insets.consumeSystemWindowInsets()
    }
}
