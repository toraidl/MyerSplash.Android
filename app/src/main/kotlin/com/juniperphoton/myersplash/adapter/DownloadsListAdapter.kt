package com.juniperphoton.myersplash.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.juniperphoton.flipperlayout.FlipperLayout
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.utils.Pasteur
import com.juniperphoton.myersplash.widget.DownloadCompleteView
import com.juniperphoton.myersplash.widget.DownloadRetryView
import com.juniperphoton.myersplash.widget.DownloadingView

class DownloadsListAdapter(private val context: Context) :
        RecyclerView.Adapter<DownloadsListAdapter.DownloadItemViewHolder>() {
    companion object {
        private const val TAG = "DownloadsListAdapter"
        private const val ITEM_TYPE_ITEM = 0
        private const val ITEM_TYPE_FOOTER = 1
        private const val MAX_DIMENSION_PREVIEW_PX = 500
    }

    interface Callback {
        fun onClickRetry(item: DownloadItem)
        fun onClickDelete(item: DownloadItem)
        fun onClickCancel(item: DownloadItem)
    }

    val data: MutableList<DownloadItem> = mutableListOf()
    var callback: Callback? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        if (position < 0 || position >= data.size) {
            return RecyclerView.NO_ID
        }
        return data[position].id.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder {
        return when (viewType) {
            ITEM_TYPE_ITEM -> {
                val view = LayoutInflater.from(context)
                        .inflate(R.layout.row_download_item, parent, false)
                DownloadItemViewHolder(view)
            }
            ITEM_TYPE_FOOTER -> {
                val footer = LayoutInflater.from(context).inflate(R.layout.row_footer_blank,
                        parent, false)
                DownloadItemViewHolder(footer)
            }
            else -> throw IllegalArgumentException("unknown view type")
        }
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
        if (getItemViewType(position) == ITEM_TYPE_FOOTER) {
            return
        }
        holder.bind(data[holder.adapterPosition])
    }

    override fun getItemCount(): Int = data.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position >= itemCount - 1) {
            ITEM_TYPE_FOOTER
        } else {
            ITEM_TYPE_ITEM
        }
    }

    fun refresh(items: List<DownloadItem>) {
        Pasteur.info(TAG, "refresh items: ${items.size}")
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    fun updateItem(item: DownloadItem) {
        val index = data.indexOf(item)
        if (index >= 0 && index <= data.size) {
            Pasteur.d(TAG, "notifyItemChanged:$index, item: $item")
            notifyItemChanged(index)
        }
    }

    inner class DownloadItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var draweeView: SimpleDraweeView? = itemView.findViewById(R.id.row_download_item_dv)
        private var flipperLayout: FlipperLayout? = itemView.findViewById(R.id.row_download_flipper_layout)
        private var downloadingView: DownloadingView? = itemView.findViewById(R.id.row_downloading_view)
        private var downloadRetryView: DownloadRetryView? = itemView.findViewById(R.id.row_download_retry_view)
        private var downloadCompleteView: DownloadCompleteView? = itemView.findViewById(R.id.row_download_complete_view)

        private var downloadItem: DownloadItem? = null

        init {
            downloadRetryView?.onClickDelete = onDelete@{
                downloadItem?.let {
                    callback?.onClickDelete(it)
                }
            }

            downloadRetryView?.onClickRetry = onRetry@{
                downloadItem?.let {
                    flipperLayout?.next(DownloadItem.DOWNLOAD_STATUS_DOWNLOADING)
                    callback?.onClickRetry(it)
                }
            }

            downloadingView?.onClickCancel = onCancel@{
                downloadItem?.let {
                    flipperLayout?.next(DownloadItem.DOWNLOAD_STATUS_FAILED)
                    callback?.onClickCancel(it)
                }
            }
        }

        internal fun bind(item: DownloadItem) {
            this.downloadItem = item

            draweeView?.let {
                val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(item.thumbUrl))
                        .setResizeOptions(ResizeOptions(MAX_DIMENSION_PREVIEW_PX,
                                MAX_DIMENSION_PREVIEW_PX)).build()
                val controller = Fresco.newDraweeControllerBuilder().setImageRequest(request)
                        .setOldController(it.controller).build()
                it.controller = controller
            }

            downloadingView?.progress = item.progress

            downloadCompleteView?.let {
                it.filePath = item.filePath
                it.setThemeBackColor(item.color)
            }

            downloadRetryView?.let {
                it.themeColor = item.color
            }

            downloadingView?.let {
                it.progress = item.progress
                it.themeColor = item.color
            }

            val last = item.lastStatus
            if (last != item.status && last != DownloadItem.DISPLAY_STATUS_NOT_SPECIFIED) {
                flipperLayout?.next(item.status)
                item.syncStatus()
            } else {
                flipperLayout?.next(item.status, false)
                item.syncStatus()
            }
        }
    }
}
