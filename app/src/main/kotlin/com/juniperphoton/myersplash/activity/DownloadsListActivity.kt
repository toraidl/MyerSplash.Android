package com.juniperphoton.myersplash.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.DownloadsListAdapter
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.service.DownloadService
import com.juniperphoton.myersplash.utils.AnalysisHelper
import com.juniperphoton.myersplash.utils.Params
import com.juniperphoton.myersplash.utils.Pasteur
import com.juniperphoton.myersplash.viewmodel.DownloadListViewModel
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindUntilEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_manage_download.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Suppress("unused")
class DownloadsListActivity : BaseActivity(), DownloadsListAdapter.Callback, CoroutineScope by MainScope() {
    companion object {
        private const val TAG = "DownloadsListActivity"
        private const val DEFAULT_SPAN = 2
        private const val SCREEN_WIDTH_WITH_DEFAULT_SPAN = 1200
        const val ACTION = "action.downloads"
    }

    private lateinit var adapter: DownloadsListAdapter
    private lateinit var viewModel: DownloadListViewModel

    private val deleteOptionsMap = mapOf(
            0 to DownloadItem.DOWNLOAD_STATUS_DOWNLOADING,
            1 to DownloadItem.DOWNLOAD_STATUS_OK,
            2 to DownloadItem.DOWNLOAD_STATUS_FAILED
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_download)

        viewModel = ViewModelProviders.of(this).get(DownloadListViewModel::class.java)

        AnalysisHelper.logEnterDownloads()
        moreFab.setOnClickListener(this)

        Looper.myQueue().addIdleHandler {
            initViews()
            false
        }
    }

    override fun onClickView(v: View) {
        when (v.id) {
            R.id.moreFab -> {
                onClickMore()
            }
        }
    }

    private fun onClickMore() {
        AnalysisHelper.logClickMoreButtonInDownloadList()

        AlertDialog.Builder(this).setTitle(R.string.clear_options_title)
                .setItems(R.array.delete_options) { _, i ->
                    runBlocking {
                        viewModel.deleteByStatus(
                                deleteOptionsMap[i] ?: DownloadItem.DOWNLOAD_STATUS_INVALID)
                    }
                }
                .setPositiveButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
    }

    private fun updateNoItemVisibility() {
        noItemView.visibility = if (adapter.data.isEmpty()) View.GONE else View.VISIBLE
    }

    private val spanCount: Int
        get() {
            val width = window.decorView.width
            return if (width <= SCREEN_WIDTH_WITH_DEFAULT_SPAN) {
                DEFAULT_SPAN
            } else {
                val min = resources.getDimensionPixelSize(R.dimen.download_item_min_width)
                (width / min)
            }
        }

    private fun initViews() = runBlocking {
        adapter = DownloadsListAdapter(this@DownloadsListActivity)
        adapter.callback = this@DownloadsListActivity

        val layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)

        downloadsList.layoutManager = layoutManager
        downloadsList.adapter = adapter

        // We don't change the item animator so we cast it directly
        (downloadsList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        updateNoItemVisibility()

        viewModel.downloadItems.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .bindUntilEvent(this@DownloadsListActivity, Lifecycle.Event.ON_DESTROY)
                .subscribe { items ->
                    Pasteur.info(TAG, "refresh items: ${items.size}")
                    adapter.refresh(items)
                }
    }

    override fun onClickRetry(item: DownloadItem) {
        launch {
            viewModel.resetItemStatus(item.id)
            adapter.updateItem(item)

            val intent = Intent(this@DownloadsListActivity, DownloadService::class.java).apply {
                putExtra(Params.NAME_KEY, item.fileName)
                putExtra(Params.URL_KEY, item.downloadUrl)
            }
            startService(intent)
        }
    }

    override fun onClickDelete(item: DownloadItem) {
        launch {
            viewModel.deleteItem(item.id)
            adapter.updateItem(item)

            val intent = Intent(this@DownloadsListActivity, DownloadService::class.java).apply {
                putExtra(Params.CANCELED_KEY, true)
                putExtra(Params.URL_KEY, item.downloadUrl)
            }

            startService(intent)
        }
    }

    override fun onClickCancel(item: DownloadItem) {
        launch {
            viewModel.updateItemStatus(item.id, DownloadItem.DOWNLOAD_STATUS_FAILED)
            adapter.updateItem(item)

            val intent = Intent(this@DownloadsListActivity, DownloadService::class.java).apply {
                putExtra(Params.CANCELED_KEY, true)
                putExtra(Params.URL_KEY, item.downloadUrl)
            }

            startService(intent)
        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int) {
        val params = moreFab.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin += bottom
        moreFab.layoutParams = params
    }
}
