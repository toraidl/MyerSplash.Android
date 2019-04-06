package com.juniperphoton.myersplash.activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.adapter.DownloadsListAdapter
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.utils.AnalysisHelper
import com.juniperphoton.myersplash.utils.Pasteur
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_manage_download.*
import java.util.*

@Suppress("unused")
class ManageDownloadActivity : BaseActivity() {
    companion object {
        private const val TAG = "ManageDownloadActivity"
        const val ACTION = "action.downloads"
    }

    private var adapter: DownloadsListAdapter? = null

    private var itemStatusChangedListener = RealmChangeListener<DownloadItem> { item ->
        Pasteur.d(TAG, "onChange")
        if (item.isValid) {
            adapter?.updateItem(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_download)

        initViews()
        AnalysisHelper.logEnterDownloads()
        moreFab.setOnClickListener(this)
    }

    override fun onDestroy() {
        RealmCache.getInstance().removeAllChangeListeners()
        RealmCache.getInstance().close()
        super.onDestroy()
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
                    val deleteStatus = when (i) {
                        0 -> DownloadItem.DOWNLOAD_STATUS_DOWNLOADING
                        1 -> DownloadItem.DOWNLOAD_STATUS_OK
                        2 -> DownloadItem.DOWNLOAD_STATUS_FAILED
                        else -> DownloadItem.DOWNLOAD_STATUS_INVALID
                    }
                    deleteFromRealm(deleteStatus)
                }
                .setPositiveButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
    }

    private fun deleteFromRealm(status: Int) {
        RealmCache.getInstance().executeTransaction {
            it.where(DownloadItem::class.java)
                    .equalTo(DownloadItem.STATUS_KEY, status)
                    .findAll()
                    .forEach { item ->
                        item.removeAllChangeListeners()
                        item.deleteFromRealm()
                    }
            initViews()
        }
    }

    private fun updateNoItemVisibility() {
        if ((adapter?.data?.size ?: 0) > 0) {
            noItemView.visibility = View.GONE
        } else {
            noItemView.visibility = View.VISIBLE
        }
    }

    private fun initViews() {
        val downloadItems = ArrayList<DownloadItem>()

        RealmCache.getInstance()
                .where(DownloadItem::class.java)
                .findAllSorted(DownloadItem.POSITION_KEY, Sort.DESCENDING)
                .forEach {
                    downloadItems.add(it)
                }

        downloadItems.forEach {
            it.addChangeListener(itemStatusChangedListener)
        }

        if (adapter == null) {
            adapter = DownloadsListAdapter(this)
        }

        adapter!!.refreshItems(downloadItems)

        val layoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == adapter!!.itemCount - 1) 2 else 1
                }
            }
        }

        downloadsList.layoutManager = layoutManager
        downloadsList.adapter = adapter

        // We don't change the item animator so we cast it directly
        (downloadsList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        updateNoItemVisibility()
    }

    override fun onApplySystemInsets(top: Int, bottom: Int) {
        val params = moreFab.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin += bottom
        moreFab.layoutParams = params
    }
}
