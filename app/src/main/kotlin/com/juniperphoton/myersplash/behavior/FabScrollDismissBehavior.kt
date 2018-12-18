package com.juniperphoton.myersplash.behavior

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FabScrollDismissBehavior(context: Context?, attrs: AttributeSet?
) : ScrollDismissBehavior<FloatingActionButton>(context, attrs) {
    override fun onScrollStatusChanged(show: Boolean, view: FloatingActionButton) {
        if (show) {
            view.show()
        } else {
            view.hide()
        }
    }
}