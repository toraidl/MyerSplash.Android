package com.juniperphoton.myersplash.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
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
import kotlinx.coroutines.runBlocking

@Suppress("unused")
class ManageDownloadActivity : BaseActivity(), DownloadsListAdapter.Callback {
    companion object {
        private const val TAG = "ManageDownloadActivity"
        private const val SPAN = 2
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

        initViews()
        AnalysisHelper.logEnterDownloads()
        moreFab.setOnClickListener(this)
    }

    override fun onClickView(v: View) {
        when (v.id) {
            R.id.moreFab -> {
                onClickMore()
            }
        }
    }

    private fun onClickMore() {
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

    private fun initViews() = runBlocking {
        adapter = DownloadsListAdapter(this@ManageDownloadActivity)
        adapter.callback = this@ManageDownloadActivity

        val layoutManager = GridLayoutManager(this@ManageDownloadActivity, SPAN).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == adapter.itemCount - 1) 2 else 1
                }
            }
        }

        downloadsList.layoutManager = layoutManager
        downloadsList.adapter = adapter

        // We don't change the item animator so we cast it directly
        (downloadsList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        updateNoItemVisibility()

        viewModel.downloadItems.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .bindUntilEvent(this@ManageDownloadActivity, Lifecycle.Event.ON_DESTROY)
                .subscribe { items ->
                    Pasteur.info(TAG, "refresh items: ${items.size}")
                    adapter.refresh(items)
                }
    }

    override fun onClickRetry(item: DownloadItem) {
        runBlocking {
            viewModel.resetItemStatus(item.id)
            adapter.updateItem(item)

            val intent = Intent(this@ManageDownloadActivity, DownloadService::class.java).apply {
                putExtra(Params.NAME_KEY, item.fileName)
                putExtra(Params.URL_KEY, item.downloadUrl)
            }
            startService(intent)
        }
    }

    override fun onClickDelete(item: DownloadItem) {
        runBlocking {
            viewModel.deleteItem(item.id)
            adapter.updateItem(item)

            val intent = Intent(this@ManageDownloadActivity, DownloadService::class.java).apply {
                putExtra(Params.CANCELED_KEY, true)
                putExtra(Params.URL_KEY, item.downloadUrl)
            }

            startService(intent)
        }
    }

    override fun onClickCancel(item: DownloadItem) {
        runBlocking {
            viewModel.updateItemStatus(item.id, DownloadItem.DOWNLOAD_STATUS_FAILED)
            adapter.updateItem(item)

            val intent = Intent(this@ManageDownloadActivity, DownloadService::class.java).apply {
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
