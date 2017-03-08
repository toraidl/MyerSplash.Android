package com.juniperphoton.myersplash.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.juniperphoton.flipperviewlib.FlipperView;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.event.DownloadStartedEvent;
import com.juniperphoton.myersplash.model.DownloadItem;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.AnimatorListenerImpl;
import com.juniperphoton.myersplash.utils.ColorUtil;
import com.juniperphoton.myersplash.utils.DownloadItemTransactionHelper;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.ToastService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@SuppressWarnings("UnusedDeclaration")
public class ImageDetailView extends FrameLayout {
    private static final String TAG = ImageDetailView.class.getName();
    private static final int RESULT_CODE = 10000;
    private static final String SHARE_TEXT = "Share %s's amazing photo from MyerSplash app. Download this photo: %s";

    private static final int DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD = 0;
    private static final int DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOADING = 1;
    private static final int DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD_OK = 2;

    private Context mContext;

    private File mCopyFileForSharing;

    private int mHeroStartY = 0;
    private int mHeroEndY = 0;

    private View mClickedView;
    private UnsplashImage mClickedImage;

    private StateListener mNavigationCallback;

    @BindView(R.id.detail_root_sv)
    ViewGroup mDetailRootScrollView;

    @BindView(R.id.detail_hero_dv)
    SimpleDraweeView mHeroDV;

    @BindView(R.id.detail_backgrd_rl)
    ViewGroup mDetailInfoRootLayout;

    @BindView(R.id.detail_img_rl)
    ViewGroup mDetailImgRL;

    @BindView(R.id.detail_name_tv)
    TextView mNameTextView;

    @BindView(R.id.detail_name_line)
    View mLineView;

    @BindView(R.id.detail_photo_by_tv)
    TextView mPhotoByTextView;

    @BindView(R.id.detail_download_fab)
    FloatingActionButton mDownloadFAB;

    @BindView(R.id.detail_cancel_download_fab)
    FloatingActionButton mCancelDownloadFAB;

    @BindView(R.id.detail_share_fab)
    FloatingActionButton mShareFAB;

    @BindView(R.id.copy_url_tv)
    TextView mCopyUrlTextView;

    @BindView(R.id.copied_url_tv)
    TextView mCopiedUrlTextView;

    @BindView(R.id.copy_url_fl)
    FrameLayout mCopyLayout;

    @BindView(R.id.copied_url_fl)
    FrameLayout mCopiedLayout;

    @BindView(R.id.copy_url_flipper_view)
    FlipperView mCopyUrlFlipperView;

    @BindView(R.id.download_flipper_view)
    FlipperView mDownloadFlipperView;

    @BindView(R.id.detail_progress_ring)
    RingProgressView mProgressView;

    @BindView(R.id.detail_set_as_fab)
    FloatingActionButton mSetAsFAB;

    private DownloadItem mAssociatedDownloadItem;

    private RealmChangeListener<DownloadItem> mListener = new RealmChangeListener<DownloadItem>() {
        @Override
        public void onChange(DownloadItem element) {
            switch (element.getStatus()) {
                case DownloadItem.DOWNLOAD_STATUS_DOWNLOADING:
                    mProgressView.setProgress(element.getProgress());
                    break;
                case DownloadItem.DOWNLOAD_STATUS_FAILED:
                    mDownloadFlipperView.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD);
                    break;
                case DownloadItem.DOWNLOAD_STATUS_OK:
                    mDownloadFlipperView.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD_OK);
                    break;
            }
        }
    };

    private boolean mAnimating;
    private boolean mCopied;

    public ImageDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.detail_content, this, true);
        ButterKnife.bind(this, this);

        initDetailViews();
    }

    public void registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @OnClick(R.id.detail_name_tv)
    void onClickName() {
        Uri uri = Uri.parse(mClickedImage.getUserHomePage());

        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();

        intentBuilder.setToolbarColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));

        intentBuilder.setStartAnimations(mContext, R.anim.in_from_right, R.anim.out_from_left);
        intentBuilder.setExitAnimations(mContext, R.anim.in_from_left, R.anim.out_from_right);

        CustomTabsIntent customTabsIntent = intentBuilder.build();

        customTabsIntent.launchUrl(mContext, uri);
    }

    @OnClick(R.id.copy_url_flipper_view)
    void onClickCopy() {
        if (mCopied) return;
        mCopied = true;

        mCopyUrlFlipperView.next();

        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(mContext.getString(R.string.app_name), mClickedImage.getDownloadUrl());
        clipboard.setPrimaryClip(clip);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                mCopyUrlFlipperView.next();
                mCopied = false;
            }
        }, 2000);
    }

    @OnClick(R.id.detail_share_fab)
    void onClickShare() {
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(
                ImageRequest.fromUri(Uri.parse(mClickedImage.getListUrl())), null);

        File localFile = null;

        if (cacheKey != null) {
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            }
        }

        boolean copied = false;
        if (localFile != null && localFile.exists()) {
            mCopyFileForSharing = new File(DownloadUtil.getGalleryPath(), "Share-" + localFile.getName());
            copied = DownloadUtil.copyFile(localFile, mCopyFileForSharing);
        }

        if (mCopyFileForSharing == null || !mCopyFileForSharing.exists() || !copied) {
            ToastService.sendShortToast(mContext.getString(R.string.something_wrong));
            return;
        }

        String shareText = String.format(SHARE_TEXT, mClickedImage.getUserName(), mClickedImage.getDownloadUrl());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/jpg");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mCopyFileForSharing));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ((Activity) mContext).startActivityForResult(Intent.createChooser(intent, "Share"), RESULT_CODE, null);
    }

    private void initDetailViews() {
        mDetailRootScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP: {
                        tryHide();
                    }
                    break;
                }
                return true;
            }
        });

        mDetailRootScrollView.setVisibility(View.INVISIBLE);

        mDetailInfoRootLayout.setTranslationY(-getResources().getDimensionPixelOffset(R.dimen.img_detail_info_height));
        mDownloadFlipperView.setTranslationX(getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_right_hide));
        mShareFAB.setTranslationX(getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_right_hide));

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 360);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgressView.setRotation((float) animation.getAnimatedValue());
            }
        });
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(1200);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.start();
    }

    private void associateWithDownloadItem(DownloadItem item) {
        if (item == null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            mAssociatedDownloadItem = realm.where(DownloadItem.class)
                    .equalTo(DownloadItem.ID_KEY, mClickedImage.getId()).findFirst();
            realm.commitTransaction();
        }
        if (mAssociatedDownloadItem != null) {
            mAssociatedDownloadItem.removeChangeListeners();
            mAssociatedDownloadItem.addChangeListener(mListener);
        }
    }

    @OnClick(R.id.detail_download_fab)
    void onClickDownload() {
        Log.d(TAG, "onClickDownload");
        if (mClickedImage == null) {
            return;
        }
        DownloadUtil.checkAndDownload((Activity) mContext, mClickedImage);
    }

    @OnClick(R.id.detail_cancel_download_fab)
    void onClickCancelDownload() {
        Log.d(TAG, "onClickCancelDownload");
        if (mClickedImage == null) {
            return;
        }
        mDownloadFlipperView.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD);

        DownloadItemTransactionHelper.updateStatus(mAssociatedDownloadItem, DownloadItem.DOWNLOAD_STATUS_FAILED);
        DownloadUtil.cancelDownload(mContext, mClickedImage);
    }

    @OnClick(R.id.detail_set_as_fab)
    void onClickSetAsFAB() {
        String url = mClickedImage.getPathForDownload();
        if (url != null) {
            File file = new File(url);
            Uri uri = FileProvider.getUriForFile(App.getInstance(), App.getInstance().getString(R.string.authorities), file);
            Intent intent = WallpaperManager.getInstance(App.getInstance()).getCropAndSetWallpaperIntent(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.getInstance().startActivity(intent);
        }
    }

    private void toggleHeroViewAnimation(int startY, int endY, final boolean show) {
        if (show) {
            mHeroStartY = startY;
            mHeroEndY = endY;
        } else {
            mDownloadFlipperView.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD);
        }

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(startY, endY);
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDetailImgRL.setTranslationY((int) animation.getAnimatedValue());
            }
        });
        valueAnimator.addListener(new AnimatorListenerImpl() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show && mClickedView != null) {
                    mClickedView.setVisibility(View.VISIBLE);
                    toggleMaskAnimation(false);
                    mClickedView = null;
                    mClickedImage = null;
                    mAnimating = false;
                } else {
                    toggleDetailRLAnimation(true);
                    toggleDownloadFlipperViewAnimation(true);
                    toggleShareBtnAnimation(true);
                }
            }
        });
        valueAnimator.start();
    }

    private void checkDownloadStatus() {
        Observable.just(mClickedImage)
                .observeOn(Schedulers.io())
                .map(new Func1<UnsplashImage, Boolean>() {
                    @Override
                    public Boolean call(UnsplashImage unsplashImage) {
                        return mClickedImage.hasDownloaded();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean b) {
                        if (b) {
                            mDownloadFlipperView.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD_OK);
                        }
                    }
                });
    }

    private int getTargetY() {
        return (((Activity) mContext).getWindow().getDecorView().getHeight() -
                (getResources().getDimensionPixelSize(R.dimen.img_detail_height))) / 2;
    }

    private void toggleDetailRLAnimation(final boolean show) {
        int startY = show ? (-getResources().getDimensionPixelOffset(R.dimen.img_detail_info_height)) : 0;
        int endY = show ? 0 : (-getResources().getDimensionPixelOffset(R.dimen.img_detail_info_height));

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(startY, endY);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDetailInfoRootLayout.setTranslationY((int) animation.getAnimatedValue());
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    toggleHeroViewAnimation(mHeroEndY, mHeroStartY, false);
                } else {
                    mAnimating = false;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    private void toggleDownloadFlipperViewAnimation(final boolean show) {
        int normalX = getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_right);

        int hideX = getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_right_hide);

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(show ? hideX : 0, show ? 0 : hideX);
        valueAnimator.setDuration(700);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDownloadFlipperView.setTranslationX((int) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

    private void toggleShareBtnAnimation(final boolean show) {
        int normalX = getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_right);

        int hideX = getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_right_hide);

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(show ? hideX : 0, show ? 0 : hideX);
        valueAnimator.setDuration(700);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mShareFAB.setTranslationX((int) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

    private void toggleMaskAnimation(final boolean show) {
        ValueAnimator animator = ValueAnimator.ofArgb(show ? Color.TRANSPARENT : ContextCompat.getColor(mContext, R.color.MaskColor),
                show ? ContextCompat.getColor(mContext, R.color.MaskColor) : Color.TRANSPARENT);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDetailRootScrollView.setBackground(new ColorDrawable((int) animation.getAnimatedValue()));
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (show) {
                    if (mNavigationCallback != null) {
                        mNavigationCallback.onShowing();
                    }
                } else {
                    if (mNavigationCallback != null) {
                        mNavigationCallback.onHiding();
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (show) {
                    if (mNavigationCallback != null) {
                        mNavigationCallback.onShown();
                    }
                } else {
                    mDetailRootScrollView.setVisibility(View.INVISIBLE);
                    if (mNavigationCallback != null) {
                        mNavigationCallback.onHidden();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private void hideDetailPanel() {
        if (mAnimating) return;
        toggleDetailRLAnimation(false);
        toggleDownloadFlipperViewAnimation(false);
        toggleShareBtnAnimation(false);
    }

    public void setNavigationCallback(StateListener callback) {
        mNavigationCallback = callback;
    }

    public void deleteShareFileInDelay() {
        //TODO: Should has a better way to do this.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCopyFileForSharing != null && mCopyFileForSharing.exists()) {
                    boolean ok = mCopyFileForSharing.delete();
                }
            }
        }, 30000);
    }

    public boolean tryHide() {
        if (mAssociatedDownloadItem != null && mAssociatedDownloadItem.isValid()) {
            mAssociatedDownloadItem.removeChangeListener(mListener);
            mAssociatedDownloadItem = null;
        }
        if (mDetailRootScrollView.getVisibility() == View.VISIBLE) {
            hideDetailPanel();
            return true;
        }
        return false;
    }

    /**
     * Show detailed image
     *
     * @param rectF         Original occupied rect
     * @param unsplashImage Image
     * @param itemView      View to be clicked
     */
    public void showDetailedImage(final RectF rectF, final UnsplashImage unsplashImage, View itemView) {
        if (mClickedView != null) {
            return;
        }
        mClickedImage = unsplashImage;
        mClickedView = itemView;
        mClickedView.setVisibility(View.INVISIBLE);

        mDetailInfoRootLayout.setBackground(new ColorDrawable(unsplashImage.getThemeColor()));
        int themeColor = unsplashImage.getThemeColor();
        int alpha = Color.alpha(themeColor);

        //Dark
        if (!ColorUtil.isColorLight(themeColor)) {
            mCopyUrlTextView.setTextColor(Color.BLACK);
            int backColor = Color.argb(200, Color.red(Color.WHITE),
                    Color.green(Color.WHITE), Color.blue(Color.WHITE));
            mCopyLayout.setBackgroundColor(backColor);
        } else {
            mCopyUrlTextView.setTextColor(Color.WHITE);
            int backColor = Color.argb(200, Color.red(Color.BLACK),
                    Color.green(Color.BLACK), Color.blue(Color.BLACK));
            mCopyLayout.setBackgroundColor(backColor);
        }

        mNameTextView.setText(unsplashImage.getUserName());
        mProgressView.setProgress(5);

        int backColor = unsplashImage.getThemeColor();
        if (!ColorUtil.isColorLight(backColor)) {
            mNameTextView.setTextColor(Color.WHITE);
            mLineView.setBackground(new ColorDrawable(Color.WHITE));
            mPhotoByTextView.setTextColor(Color.WHITE);
        } else {
            mNameTextView.setTextColor(Color.BLACK);
            mLineView.setBackground(new ColorDrawable(Color.BLACK));
            mPhotoByTextView.setTextColor(Color.BLACK);
        }

        mHeroDV.setImageURI(unsplashImage.getListUrl());
        mDetailRootScrollView.setVisibility(View.VISIBLE);

        int[] heroImagePosition = new int[2];
        mDetailImgRL.getLocationOnScreen(heroImagePosition);

        int itemY = (int) rectF.top;

        mAssociatedDownloadItem = DownloadUtil.getDownloadItemById(unsplashImage.getId());
        if (mAssociatedDownloadItem != null) {
            Log.d(TAG, "found down item,status:" + mAssociatedDownloadItem.getStatus());
            switch (mAssociatedDownloadItem.getStatus()) {
                case DownloadItem.DOWNLOAD_STATUS_DOWNLOADING:
                    mDownloadFlipperView.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOADING);
                    mProgressView.setProgress(mAssociatedDownloadItem.getProgress());
                    break;
                case DownloadItem.DOWNLOAD_STATUS_FAILED:
                    break;
                case DownloadItem.DOWNLOAD_STATUS_OK:
                    if (mClickedImage.hasDownloaded()) {
                        mDownloadFlipperView.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOAD_OK);
                    }
                    break;
            }
            associateWithDownloadItem(mAssociatedDownloadItem);
        }

        int targetPositionY = getTargetY();

        checkDownloadStatus();

        toggleMaskAnimation(true);
        toggleHeroViewAnimation(itemY, targetPositionY, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receivedDownloadStarted(DownloadStartedEvent event) {
        if (mClickedImage != null && event.id.equals(mClickedImage.getId())) {
            mDownloadFlipperView.next(DOWNLOAD_FLIPPER_VIEW_STATUS_DOWNLOADING);
            associateWithDownloadItem(null);
        }
    }

    public interface StateListener {
        void onShowing();

        void onHiding();

        void onShown();

        void onHidden();
    }
}
