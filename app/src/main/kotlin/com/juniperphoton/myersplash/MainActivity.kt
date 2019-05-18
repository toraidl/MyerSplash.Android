package com.juniperphoton.myersplash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.juniperphoton.myersplash.activity.BaseActivity
import com.juniperphoton.myersplash.activity.DownloadsListActivity
import com.juniperphoton.myersplash.adapter.MainListFragmentAdapter
import com.juniperphoton.myersplash.event.ScrollToTopEvent
import com.juniperphoton.myersplash.extension.getStatusBarHeight
import com.juniperphoton.myersplash.extension.pow
import com.juniperphoton.myersplash.extension.startServiceSafely
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.service.DownloadService
import com.juniperphoton.myersplash.utils.AnalysisHelper
import com.juniperphoton.myersplash.utils.Params
import com.juniperphoton.myersplash.utils.PermissionUtils
import com.juniperphoton.myersplash.widget.PivotTitleBar
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus

class MainActivity : BaseActivity() {
    companion object {
        private const val SAVED_NAVIGATION_INDEX = "navigation_index"
        private const val DOWNLOADS_SHORTCUT_ID = "downloads_shortcut"

        private const val ACTION_SEARCH = "action.search"
        private const val ACTION_DOWNLOADS = "action.download"

        private val ID_MAPS = mutableMapOf(
                0 to UnsplashCategory.NEW_CATEGORY_ID,
                1 to UnsplashCategory.FEATURED_CATEGORY_ID,
                2 to UnsplashCategory.HIGHLIGHTS_CATEGORY_ID)
    }

    private var mainListFragmentAdapter: MainListFragmentAdapter? = null

    private var handleShortcut: Boolean = false
    private var initNavigationIndex = PivotTitleBar.DEFAULT_SELECTED
    private var fabPositionX: Int = 0
    private var fabPositionY: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleShortcutsAction()

        if (savedInstanceState != null) {
            initNavigationIndex = savedInstanceState.getInt(SAVED_NAVIGATION_INDEX,
                    PivotTitleBar.DEFAULT_SELECTED)
        }

        initShortcuts()
        initMainViews()
        startServiceToCheck()
    }

    private fun startServiceToCheck() {
        val intent = Intent(this, DownloadService::class.java).apply {
            putExtra(Params.CHECK_STATUS, true)
        }
        startServiceSafely(intent)
    }

    private fun initShortcuts() {
        @TargetApi(android.os.Build.VERSION_CODES.N_MR1)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            if (shortcutManager.dynamicShortcuts.size > 0) {
                shortcutManager.removeAllDynamicShortcuts()
            }
            val intent = Intent(this, DownloadsListActivity::class.java)
            intent.action = DownloadsListActivity.ACTION
            val shortcut = ShortcutInfo.Builder(this, DOWNLOADS_SHORTCUT_ID)
                    .setShortLabel(getString(R.string.downloadLowercase))
                    .setLongLabel(getString(R.string.downloadLowercase))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_download_shortcut))
                    .setIntent(intent)
                    .build()
            shortcutManager.dynamicShortcuts = listOf(shortcut)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        val index = viewPager.currentItem
        if (index in 0..2) {
            outState?.putInt(SAVED_NAVIGATION_INDEX, viewPager.currentItem)
        }
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onResume() {
        super.onResume()
        PermissionUtils.checkAndRequest(this@MainActivity)
    }

    private fun toggleSearchView(show: Boolean, useAnimation: Boolean) {
        if (show) {
            searchFab.hide()
        } else {
            AnalysisHelper.logEnterSearch()
            searchFab.show()
        }

        val location = IntArray(2)
        searchFab.getLocationOnScreen(location)

        if (show) {
            fabPositionX = (location[0] + searchFab.width / 2f).toInt()
            fabPositionY = (location[1] + searchFab.height / 2f).toInt()
        }

        val width = window.decorView.width
        val height = window.decorView.height

        val radius = Math.sqrt(width.pow() + height.pow()).toInt()
        val animator = ViewAnimationUtils.createCircularReveal(searchView,
                fabPositionX, fabPositionY,
                (if (show) 0 else radius).toFloat(), (if (show) radius else 0).toFloat())
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(a: Animator) {
                if (!show) {
                    searchView.reset()
                    searchView.visibility = View.GONE
                } else {
                    searchView.onShown()
                }
            }
        })

        searchView.visibility = View.VISIBLE

        if (show) {
            searchView.tryShowKeyboard()
            searchView.onShowing()
        } else {
            searchView.onHiding()
        }
        if (useAnimation) {
            animator.start()
        }
    }

    private fun initMainViews() {
        imageDetailView.apply {
            onShowing = {
                searchFab.hide()
            }
            onHidden = {
                searchFab.show()
                if (toolbarLayout.height - Math.abs(toolbarLayout.top) < 0.01) {
                    tagView.animate().alpha(1f).setDuration(300).start()
                }
            }
        }

        searchFab.setOnClickListener {
            toggleSearchView(show = true, useAnimation = true)
        }

        pivotTitleBar.apply {
            val lp = pivotTitleBar.layoutParams as ViewGroup.MarginLayoutParams
            lp.topMargin = getStatusBarHeight()
            layoutParams = lp

            onSingleTap = {
                viewPager.currentItem = it
                EventBus.getDefault().post(ScrollToTopEvent(ID_MAPS[it]!!, false))
            }
            onDoubleTap = {
                viewPager.currentItem = it
                EventBus.getDefault().post(ScrollToTopEvent(ID_MAPS[it]!!, true))
            }
            selectedItem = initNavigationIndex
        }

        mainListFragmentAdapter = MainListFragmentAdapter({ rectF, unsplashImage, itemView ->
            val location = IntArray(2)
            tagView.getLocationOnScreen(location)
            if (rectF.top <= location[1] + tagView.height) {
                tagView.animate().alpha(0f).setDuration(100).start()
            }
            imageDetailView.show(rectF, unsplashImage, itemView)
        }, supportFragmentManager)

        viewPager.apply {
            adapter = mainListFragmentAdapter
            currentItem = initNavigationIndex
            offscreenPageLimit = 3
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int,
                                            positionOffset: Float,
                                            positionOffsetPixels: Int) = Unit

                override fun onPageSelected(position: Int) {
                    pivotTitleBar.selectedItem = position

                    val title = "# ${pivotTitleBar.selectedString}"
                    tagView.text = title
                    AnalysisHelper.logTabSelected(title)
                }

                override fun onPageScrollStateChanged(state: Int) = Unit
            })
        }

        tagView.text = "# ${getString(R.string.pivot_new)}"

        toolbarLayout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                    if (Math.abs(verticalOffset) - appBarLayout.height == 0) {
                        //todo extract duration
                        tagView.animate().alpha(1f).setDuration(300).start()
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
                        searchFab.hide()
                    } else {
                        tagView.animate().alpha(0f).setDuration(100).start()
                        window.decorView.systemUiVisibility = 0
                        searchFab.show()
                    }
                })

        tagView.setOnClickListener {
            EventBus.getDefault().post(ScrollToTopEvent(ID_MAPS[pivotTitleBar.selectedItem]!!, false))
        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int) {
        val params = searchFab.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin += bottom
        searchFab.layoutParams = params
    }

    private fun handleShortcutsAction() {
        if (handleShortcut) {
            return
        }
        val action = intent.action
        if (action != null) {
            when (action) {
                ACTION_SEARCH -> {
                    handleShortcut = true
                    toolbarLayout.post { toggleSearchView(show = true, useAnimation = false) }
                }
                ACTION_DOWNLOADS -> {
                    val intent = Intent(this, DownloadsListActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (searchView.visibility == View.VISIBLE) {
            if (searchView.tryHide()) {
                return
            }
            toggleSearchView(false, true)
            return
        }
        if (imageDetailView.tryHide()) {
            return
        }
        super.onBackPressed()
    }
}
