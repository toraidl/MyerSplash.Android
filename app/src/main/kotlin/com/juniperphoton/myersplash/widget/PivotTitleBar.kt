package com.juniperphoton.myersplash.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.*
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.activity.AboutActivity
import com.juniperphoton.myersplash.activity.DownloadsListActivity
import com.juniperphoton.myersplash.activity.SettingsActivity

typealias OnItemSelectedListener = ((Int) -> Unit)

@Suppress("UNUSED")
@SuppressLint("ClickableViewAccessibility")
class PivotTitleBar(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    companion object {
        const val DEFAULT_SELECTED = 0
        private const val ANIMATION_DURATION_MILLIS = 300L
    }

    @BindView(R.id.more_btn)
    lateinit var moreBtn: View

    @BindView(R.id.pivot_item_0)
    lateinit var item0: TextView

    @BindView(R.id.pivot_item_1)
    lateinit var item1: TextView

    @BindView(R.id.pivot_item_2)
    lateinit var item2: TextView

    /**
     * Invoked when a single tap is performed on an item.
     */
    var onSingleTap: OnItemSelectedListener? = null

    /**
     * Invoked when a double tap is performed on an item.
     */
    var onDoubleTap: OnItemSelectedListener? = null

    /**
     * Which item is current selected.
     */
    var selectedItem = DEFAULT_SELECTED
        set(value) {
            toggleAnimation(selectedItem, value)
            field = value
        }

    /**
     * Which item name is current selected.
     */
    val selectedString: String
        get() = when (selectedItem) {
            1 -> context.getString(R.string.pivot_featured).toUpperCase()
            2 -> context.getString(R.string.pivot_highlights).toUpperCase()
            else -> context.getString(R.string.pivot_new).toUpperCase()
        }

    private var touchingViewIndex: Int = 0

    private lateinit var gestureDetector: GestureDetector

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            onSingleTap?.invoke(touchingViewIndex)
            return super.onSingleTapConfirmed(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleTap?.invoke(touchingViewIndex)
            return super.onDoubleTap(e)
        }
    }

    private val onTouchListener = OnTouchListener { v, event ->
        when (v) {
            item0 -> touchingViewIndex = 0
            item1 -> touchingViewIndex = 1
            item2 -> touchingViewIndex = 2
        }
        gestureDetector.onTouchEvent(event)
        true
    }

    private val menuMap: Map<Int, Class<out Any>> = mapOf(
            R.id.menu_settings to SettingsActivity::class.java,
            R.id.menu_downloads to DownloadsListActivity::class.java,
            R.id.menu_about to AboutActivity::class.java
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.pivot_layout, this, true)
        ButterKnife.bind(this)

        gestureDetector = GestureDetector(context, gestureListener)
        item0.setOnTouchListener(onTouchListener)
        item1.setOnTouchListener(onTouchListener)
        item2.setOnTouchListener(onTouchListener)
    }

    // Must follow the init block to initialize item0, item1 and item2.
    private val itemsMap: Map<Int, View> = mapOf(
            0 to item0,
            1 to item1,
            2 to item2
    )

    private fun toggleAnimation(prevIndex: Int, newIndex: Int) {
        val prevView = itemsMap[prevIndex]
        val nextView = itemsMap[newIndex]

        prevView?.animate()?.alpha(0.3f)?.setDuration(ANIMATION_DURATION_MILLIS)?.start()
        nextView?.animate()?.alpha(1f)?.setDuration(ANIMATION_DURATION_MILLIS)?.start()
    }

    @OnClick(R.id.more_btn)
    fun onClickMore() {
        val popupMenu = PopupMenu(context, moreBtn)
        popupMenu.inflate(R.menu.main)
        popupMenu.gravity = Gravity.END
        popupMenu.setOnMenuItemClickListener { item ->
            val intent: Intent?
            intent = Intent(context, menuMap[item.itemId])
            context.startActivity(intent)
            true
        }
        popupMenu.show()
    }
}