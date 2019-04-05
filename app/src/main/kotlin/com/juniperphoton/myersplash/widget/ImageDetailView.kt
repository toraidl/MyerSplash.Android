package com.juniperphoton.myersplash.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.juniperphoton.flipperlayout.FlipperLayout
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.activity.EditActivity
import com.juniperphoton.myersplash.event.DownloadStartedEvent
import com.juniperphoton.myersplash.extension.isLightColor
import com.juniperphoton.myersplash.extension.toHexString
import com.juniperphoton.myersplash.fragment.Action
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.*
import io.reactivex.MaybeObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.RealmChangeListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

@Suppress("unused")
class ImageDetailView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    companion object {
        private const val TAG = "ImageDetailView"
        private const val RESULT_CODE = 10000

        private const val DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD = 0
        private const val DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOADING = 1
        private const val DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD_OK = 2

        private const val RESET_THRESHOLD = 150
        private const val MOVE_THRESHOLD = 10

        private const val ANIMATION_DURATION_FAST_MILLIS = 300L
        private const val ANIMATION_DURATION_SLOW_MILLIS = 400L
        private const val ANIMATION_DURATION_VERY_SLOW_MILLIS = 500L

        private const val URL_COPIED_DELAY_MILLIS = 2000L
    }

    private var listPositionY = 0f

    private var clickedView: View? = null
    private var clickedImage: UnsplashImage? = null

    /**
     * Invoked when the display animation is started.
     */
    var onShowing: Action? = null

    /**
     * Invoke when the view is fully displayed.
     */
    var onShown: Action? = null

    /**
     * Invoked when the view is about to hide.
     */
    var onHiding: Action? = null

    /**
     * Invoked when the view is invisible to user.
     */
    var onHidden: Action? = null

    @BindView(R.id.detail_root_sv)
    lateinit var detailRootScrollView: ViewGroup

    @BindView(R.id.detail_hero_view)
    lateinit var heroView: SimpleDraweeView

    @BindView(R.id.detail_backgrd_rl)
    lateinit var detailInfoRootLayout: ViewGroup

    @BindView(R.id.detail_img_rl)
    lateinit var detailImgRL: ViewGroup

    @BindView(R.id.detail_name_tv)
    lateinit var nameTextView: TextView

    @BindView(R.id.detail_name_line)
    lateinit var lineView: View

    @BindView(R.id.detail_photo_by_tv)
    lateinit var photoByTextView: TextView

    @BindView(R.id.detail_download_fab)
    lateinit var downloadFAB: FloatingActionButton

    @BindView(R.id.detail_cancel_download_fab)
    lateinit var cancelDownloadFAB: FloatingActionButton

    @BindView(R.id.detail_share_fab)
    lateinit var shareFAB: FloatingActionButton

    @BindView(R.id.copy_url_tv)
    lateinit var copyUrlTextView: TextView

    @BindView(R.id.copied_url_tv)
    lateinit var copiedUrlTextView: TextView

    @BindView(R.id.copy_url_fl)
    lateinit var copyLayout: FrameLayout

    @BindView(R.id.copied_url_fl)
    lateinit var copiedLayout: FrameLayout

    @BindView(R.id.copy_url_flipper_layout)
    lateinit var copyUrlFlipperLayout: FlipperLayout

    @BindView(R.id.download_flipper_layout)
    lateinit var downloadFlipperLayout: FlipperLayout

    @BindView(R.id.detail_progress_ring)
    lateinit var progressView: RingProgressView

    @BindView(R.id.detail_set_as_fab)
    lateinit var setAsFAB: FloatingActionButton

    private var associatedDownloadItem: DownloadItem? = null

    private val realmChangeListener = RealmChangeListener<DownloadItem> { element ->
        when (element.status) {
            DownloadItem.DOWNLOAD_STATUS_DOWNLOADING -> progressView.progress = element.progress
            DownloadItem.DOWNLOAD_STATUS_FAILED -> downloadFlipperLayout.next(DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD)
            DownloadItem.DOWNLOAD_STATUS_OK -> downloadFlipperLayout.next(DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD_OK)
        }
    }

    private val shareButtonHideOffset: Int
        get() = resources.getDimensionPixelOffset(R.dimen.share_btn_margin_right_hide)

    private val downloadFlipperLayoutHideOffset: Int
        get() = resources.getDimensionPixelOffset(R.dimen.download_btn_margin_right_hide)

    private var animating: Boolean = false
    private var copied: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.detail_content, this, true)
        ButterKnife.bind(this, this)

        initDetailViews()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initDetailViews() {
        detailRootScrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                tryHide()
            }
            true
        }

        detailRootScrollView.visibility = View.INVISIBLE

        detailInfoRootLayout.translationY = (-resources.getDimensionPixelOffset(R.dimen.img_detail_info_height)).toFloat()
        downloadFlipperLayout.translationX = resources.getDimensionPixelOffset(R.dimen.download_btn_margin_right_hide).toFloat()
        shareFAB.translationX = resources.getDimensionPixelOffset(R.dimen.share_btn_margin_right_hide).toFloat()

        ValueAnimator.ofFloat(0f, 360f).apply {
            addUpdateListener { animation -> progressView.rotation = animation.animatedValue as Float }
            interpolator = LinearInterpolator()
            duration = 1200
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            start()
        }

        heroView.setOnTouchListener { _, e ->
            if (animating) {
                return@setOnTouchListener false
            }
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    downY = e.rawY

                    startX = detailImgRL.translationX
                    startY = detailImgRL.translationY

                    pointerDown = true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!pointerDown) {
                        return@setOnTouchListener false
                    }
                    if (downX == 0f || downY == 0f) {
                        downX = e.rawX
                        downY = e.rawY

                        startX = detailImgRL.translationX
                        startY = detailImgRL.translationY
                    }

                    val dx = e.rawX - downX
                    val dy = e.rawY - downY

                    if (Math.abs(dx) >= MOVE_THRESHOLD || Math.abs(dy) >= MOVE_THRESHOLD) {
                        toggleFadeAnimation(false)
                    }

                    detailImgRL.translationX = startX + dx
                    detailImgRL.translationY = startY + dy
                }
                MotionEvent.ACTION_UP -> {
                    if (!pointerDown) {
                        return@setOnTouchListener false
                    }

                    if (Math.abs(e.rawY - downY) >= RESET_THRESHOLD || Math.abs(e.rawX - downX) >= RESET_THRESHOLD) {
                        tryHide()
                    } else {
                        detailImgRL.animate().translationX(startX).translationY(startY).setDuration(ANIMATION_DURATION_FAST_MILLIS).start()
                        toggleFadeAnimation(true)
                    }

                    pointerDown = false
                }
            }
            true
        }
    }

    private var downX: Float = 0f
    private var downY: Float = 0f

    private var startX: Float = 0f
    private var startY: Float = 0f

    private var pointerDown: Boolean = false

    private fun toggleFadeAnimation(show: Boolean) {
        if (show) {
            if (detailInfoRootLayout.alpha == 1f) {
                return
            }
        } else if (detailInfoRootLayout.alpha == 0f) {
            return
        }

        val valueAnimator = ValueAnimator.ofFloat(if (show) 1f else 0f)
        valueAnimator.addUpdateListener {
            detailInfoRootLayout.alpha = it.animatedValue as Float
            shareFAB.alpha = it.animatedValue as Float
            downloadFlipperLayout.alpha = it.animatedValue as Float
        }
        valueAnimator.setDuration(ANIMATION_DURATION_FAST_MILLIS).start()
    }

    private fun resetStatus() {
        shareFAB.alpha = 1f
        detailInfoRootLayout.alpha = 1f
        downloadFlipperLayout.alpha = 1f

        shareFAB.translationX = shareButtonHideOffset.toFloat()
        downloadFlipperLayout.translationX = downloadFlipperLayoutHideOffset.toFloat()
    }

    private fun associateWithDownloadItem(item: DownloadItem?) {
        if (item == null) {
            RealmCache.getInstance().executeTransaction {
                associatedDownloadItem = it.where(DownloadItem::class.java)
                        .equalTo(DownloadItem.ID_KEY, clickedImage!!.id).findFirst()
            }
        }

        associatedDownloadItem?.removeAllChangeListeners()
        associatedDownloadItem?.addChangeListener(realmChangeListener)
    }

    private fun toggleHeroViewAnimation(startY: Float, endY: Float, show: Boolean) {
        if (!show) {
            downloadFlipperLayout.next(DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD)
        } else {
            detailImgRL.translationX = 0f
        }

        val startX = detailImgRL.translationX

        ValueAnimator.ofFloat(startY, endY).apply {
            duration = ANIMATION_DURATION_FAST_MILLIS
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                detailImgRL.translationX = startX * (1 - it.animatedFraction)
                detailImgRL.translationY = it.animatedValue as Float
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(a: Animator) {
                    if (!show && clickedView != null) {
                        clickedView!!.visibility = View.VISIBLE
                        toggleMaskAnimation(false)
                        clickedView = null
                        clickedImage = null
                        animating = false
                    } else {
                        toggleDetailRLAnimation(show = true, oneshot = false)
                        toggleDownloadFlipperLayoutAnimation(show = true, oneshot = false)
                        toggleShareBtnAnimation(show = true, oneshot = false)
                    }
                }
            })
            start()
        }
    }

    private fun checkDownloadStatus(item: DownloadItem) {
        val file = File(item.filePath)
        if (file.exists() && file.canRead()) {
            downloadFlipperLayout.next(DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD_OK)
        }
    }

    private val targetY: Float
        get() = ((context as Activity).window.decorView.height
                - resources.getDimensionPixelSize(R.dimen.img_detail_height)) / 2f

    private fun toggleDetailRLAnimation(show: Boolean, oneshot: Boolean) {
        val startY = if (show) -resources.getDimensionPixelOffset(R.dimen.img_detail_info_height) else 0
        val endY = if (show) 0 else -resources.getDimensionPixelOffset(R.dimen.img_detail_info_height)

        ValueAnimator().apply {
            setFloatValues(startY.toFloat(), endY.toFloat())
            duration = if (oneshot) 0 else ANIMATION_DURATION_SLOW_MILLIS
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { animation ->
                detailInfoRootLayout.translationY = animation.animatedValue as Float
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(a: Animator) {
                    animating = true
                }

                override fun onAnimationEnd(a: Animator) {
                    if (!show) {
                        toggleHeroViewAnimation(detailImgRL.translationY, listPositionY, false)
                    } else {
                        animating = false
                    }
                }
            })
            start()
        }
    }

    private fun toggleDownloadFlipperLayoutAnimation(show: Boolean, oneshot: Boolean) {
        val hideX = downloadFlipperLayoutHideOffset

        val start = if (show) hideX else 0
        val end = if (show) 0 else hideX

        ValueAnimator().apply {
            setFloatValues(start.toFloat(), end.toFloat())
            duration = if (oneshot) 0 else ANIMATION_DURATION_VERY_SLOW_MILLIS
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation -> downloadFlipperLayout.translationX = animation.animatedValue as Float }
            start()
        }
    }

    private fun toggleShareBtnAnimation(show: Boolean, oneshot: Boolean) {
        val hideX = shareButtonHideOffset

        val start = if (show) hideX else 0
        val end = if (show) 0 else hideX

        ValueAnimator().apply {
            setFloatValues(start.toFloat(), end.toFloat())
            duration = if (oneshot) 0 else ANIMATION_DURATION_VERY_SLOW_MILLIS
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation -> shareFAB.translationX = animation.animatedValue as Float }
            start()
        }
    }

    private fun toggleMaskAnimation(show: Boolean) {
        val animator = ValueAnimator.ofArgb(if (show) Color.TRANSPARENT else ContextCompat.getColor(context, R.color.MaskColor),
                if (show) ContextCompat.getColor(context, R.color.MaskColor) else Color.TRANSPARENT)
        animator.duration = ANIMATION_DURATION_FAST_MILLIS
        animator.addUpdateListener { animation -> detailRootScrollView.background = ColorDrawable(animation.animatedValue as Int) }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(a: Animator) {
                if (show) {
                    onShowing?.invoke()
                } else {
                    onHiding?.invoke()
                }
            }

            override fun onAnimationEnd(a: Animator) {
                if (show) {
                    onShown?.invoke()
                } else {
                    resetStatus()
                    detailRootScrollView.visibility = View.INVISIBLE
                    onHidden?.invoke()
                }
            }
        })
        animator.start()
    }

    private fun hideDetailPanel() {
        if (animating) return

        val oneshot = detailInfoRootLayout.alpha == 0f
        toggleDetailRLAnimation(false, oneshot)
        toggleDownloadFlipperLayoutAnimation(false, oneshot)
        toggleShareBtnAnimation(false, oneshot)
    }

    private var disposable: Disposable? = null

    private fun extractThemeColor(image: UnsplashImage) {
        PaletteUtil.extractThemeColorFromUnsplashImage(image)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MaybeObserver<Int> {
                    override fun onSuccess(color: Int) {
                        image.color = color.toHexString()
                        updateThemeColor(color)
                    }

                    override fun onComplete() {
                        updateThemeColor(Color.BLACK)
                    }

                    override fun onSubscribe(d: Disposable) {
                        disposable = d
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        onComplete()
                    }
                })
    }

    private fun updateThemeColor(themeColor: Int) {
        detailInfoRootLayout.background = ColorDrawable(themeColor)
        // change the color
        if (!themeColor.isLightColor()) {
            copyUrlTextView.setTextColor(Color.BLACK)
            val backColor = Color.argb(200, Color.red(Color.WHITE),
                    Color.green(Color.WHITE), Color.blue(Color.WHITE))
            copyLayout.setBackgroundColor(backColor)

            nameTextView.setTextColor(Color.WHITE)
            lineView.background = ColorDrawable(Color.WHITE)
            photoByTextView.setTextColor(Color.WHITE)
        } else {
            copyUrlTextView.setTextColor(Color.WHITE)
            val backColor = Color.argb(200, Color.red(Color.BLACK),
                    Color.green(Color.BLACK), Color.blue(Color.BLACK))
            copyLayout.setBackgroundColor(backColor)

            nameTextView.setTextColor(Color.BLACK)
            lineView.background = ColorDrawable(Color.BLACK)
            photoByTextView.setTextColor(Color.BLACK)
        }
    }

    fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @OnClick(R.id.detail_name_tv)
    fun onClickName() {
        clickedImage?.userHomePage?.let {
            val uri = Uri.parse(it)

            val intentBuilder = CustomTabsIntent.Builder()

            intentBuilder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
            intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))

            intentBuilder.setStartAnimations(context, R.anim.in_from_right, R.anim.out_from_left)
            intentBuilder.setExitAnimations(context, R.anim.in_from_left, R.anim.out_from_right)

            val customTabsIntent = intentBuilder.build()

            customTabsIntent.launchUrl(context, uri)
        }
    }

    @OnClick(R.id.copy_url_flipper_layout)
    fun onClickCopy() {
        if (copied) return
        copied = true

        copyUrlFlipperLayout.next()

        AnalysisHelper.logClickCopyUrl()

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(context.getString(R.string.app_name), clickedImage?.downloadUrl)
        clipboard.primaryClip = clip

        postDelayed({
            copyUrlFlipperLayout.next()
            copied = false
        }, URL_COPIED_DELAY_MILLIS)
    }

    @OnClick(R.id.detail_share_fab)
    fun onClickShare() {
        val file = FileUtil.getCachedFile(clickedImage!!.listUrl!!)

        if (file == null || !file.exists()) {
            ToastService.sendShortToast(context.getString(R.string.something_wrong))
            return
        }

        val shareText = context.getString(R.string.share_text, clickedImage!!.userName, clickedImage!!.downloadUrl)

        val intent = Intent(Intent.ACTION_SEND)
        val contentUri = FileProvider.getUriForFile(context,
                context.getString(R.string.authorities), file)
        intent.apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Share")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share"))
    }

    @OnClick(R.id.detail_download_fab)
    fun onClickDownload() {
        AnalysisHelper.logClickDownloadInDetails()
        if (clickedImage == null) {
            return
        }
        DownloadUtil.download(context as Activity, clickedImage!!)
    }

    @OnClick(R.id.detail_cancel_download_fab)
    fun onClickCancelDownload() {
        AnalysisHelper.logClickCancelDownloadInDetails()
        if (clickedImage == null) {
            return
        }
        downloadFlipperLayout.next(DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD)

        DownloadItemTransactionUtil.updateStatus(associatedDownloadItem!!, DownloadItem.DOWNLOAD_STATUS_FAILED)
        DownloadUtil.cancelDownload(context, clickedImage!!)
    }

    @OnClick(R.id.detail_set_as_fab)
    fun onClickSetAsFAB() {
        AnalysisHelper.logClickSetAsInDetails()
        if (associatedDownloadItem == null) {
            return
        }
        val url = "${associatedDownloadItem!!.filePath}"
        val intent = Intent(context, EditActivity::class.java)
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(url)))
        context.startActivity(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receivedDownloadStarted(event: DownloadStartedEvent) {
        if (clickedImage != null && event.id == clickedImage?.id) {
            downloadFlipperLayout.next(DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOADING)
            associateWithDownloadItem(null)
        }
    }

    /**
     * Show detailed image
     * @param rectF         rect of original image position
     * @param unsplashImage clicked image
     * @param itemView      clicked view
     */
    fun show(rectF: RectF, unsplashImage: UnsplashImage, itemView: View) {
        AnalysisHelper.logToggleImageDetails()

        if (clickedView != null) {
            return
        }
        clickedImage = unsplashImage
        clickedView = itemView
        clickedView!!.visibility = View.INVISIBLE

        val themeColor = unsplashImage.themeColor

        if (!clickedImage!!.isUnsplash) {
            photoByTextView.text = context.getString(R.string.recommended_by)

            extractThemeColor(unsplashImage)
        } else {
            photoByTextView.text = context.getString(R.string.photo_by)
            detailInfoRootLayout.background = ColorDrawable(themeColor)
        }

        updateThemeColor(themeColor)

        nameTextView.text = unsplashImage.userName
        progressView.progress = 5

        heroView.setImageURI(unsplashImage.listUrl)
        detailRootScrollView.visibility = View.VISIBLE

        val heroImagePosition = IntArray(2)
        detailImgRL.getLocationOnScreen(heroImagePosition)

        listPositionY = rectF.top

        associatedDownloadItem = DownloadUtil.getDownloadItemById(unsplashImage.id)
        if (associatedDownloadItem != null) {
            Pasteur.d(TAG, "found download item,status:" + associatedDownloadItem!!.status)
            when (associatedDownloadItem?.status) {
                DownloadItem.DOWNLOAD_STATUS_DOWNLOADING -> {
                    downloadFlipperLayout.next(DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOADING)
                    progressView.progress = associatedDownloadItem!!.progress
                }
                DownloadItem.DOWNLOAD_STATUS_FAILED -> {
                }
                DownloadItem.DOWNLOAD_STATUS_OK -> {
                    checkDownloadStatus(associatedDownloadItem!!)
                }
            }
            associateWithDownloadItem(associatedDownloadItem)
        }

        toggleMaskAnimation(true)
        toggleHeroViewAnimation(listPositionY, targetY, true)
    }

    /**
     * Try to hide this view. If this view is fully displayed to user.
     */
    fun tryHide(): Boolean {
        disposable?.dispose()
        if (associatedDownloadItem?.isValid == true) {
            associatedDownloadItem!!.removeChangeListener(realmChangeListener)
            associatedDownloadItem = null
        }
        if (detailRootScrollView.visibility == View.VISIBLE) {
            hideDetailPanel()
            return true
        }
        return false
    }
}
