package com.juniperphoton.myersplash.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.juniperphoton.flipperlayout.FlipperLayout
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.service.DownloadService
import com.juniperphoton.myersplash.utils.DownloadItemTransactionUtil
import com.juniperphoton.myersplash.utils.Params
import com.juniperphoton.myersplash.utils.Pasteur
import com.juniperphoton.myersplash.widget.DownloadCompleteView
import com.juniperphoton.myersplash.widget.DownloadRetryView
import com.juniperphoton.myersplash.widget.DownloadingView

class DownloadsListAdapter(private val context: Context) :
        androidx.recyclerview.widget.RecyclerView.Adapter<DownloadsListAdapter.DownloadItemViewHolder>() {
    companion object {
        private const val TAG = "DownloadsListAdapter"
        private const val ITEM_TYPE_ITEM = 0
        private const val ITEM_TYPE_FOOTER = 1
        private const val ASPECT_RATIO = 1.7f
        private const val MAX_DIMENSION_PREVIEW_PX = 500
    }

    var data: MutableList<DownloadItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder {
        return when (viewType) {
            ITEM_TYPE_ITEM -> {
                val view = LayoutInflater.from(context)
                        .inflate(R.layout.row_download_item, parent, false)
                val width = context.resources.displayMetrics.widthPixels
                val params = view.layoutParams.apply {
                    height = (width / ASPECT_RATIO).toInt()
                }
                view.layoutParams = params
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

    fun updateItem(item: DownloadItem) {
        val index = data.indexOf(item)
        if (index >= 0 && index <= data.size) {
            Pasteur.d(TAG, "notifyItemChanged:$index")
            notifyItemChanged(index)
        }
    }

    fun refreshItems(items: MutableList<DownloadItem>) {
        data = items
        notifyDataSetChanged()
    }

    inner class DownloadItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.row_download_item_dv)
        @JvmField
        var draweeView: SimpleDraweeView? = null

        @BindView(R.id.row_download_flipper_layout)
        @JvmField
        var flipperLayout: FlipperLayout? = null

        @BindView(R.id.row_downloading_view)
        @JvmField
        var downloadingView: DownloadingView? = null

        @BindView(R.id.row_download_retry_view)
        @JvmField
        var downloadRetryView: DownloadRetryView? = null

        @BindView(R.id.row_download_complete_view)
        @JvmField
        var downloadCompleteView: DownloadCompleteView? = null

        private var downloadItem: DownloadItem? = null

        init {
            ButterKnife.bind(this, itemView)
            downloadRetryView?.onClickDelete = onDelete@ {
                val item = downloadItem ?: return@onDelete
                try {
                    data.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)

                    val intent = Intent(App.instance, DownloadService::class.java)
                    intent.putExtra(Params.CANCELED_KEY, true)
                    intent.putExtra(Params.URL_KEY, item.downloadUrl)
                    context.startService(intent)

                    DownloadItemTransactionUtil.delete(item)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            downloadRetryView?.onClickRetry = onRetry@ {
                val item = downloadItem ?: return@onRetry

                DownloadItemTransactionUtil.updateStatus(item,
                        DownloadItem.DOWNLOAD_STATUS_DOWNLOADING)
                flipperLayout?.next(item.status)

                val intent = Intent(context, DownloadService::class.java)
                intent.putExtra(Params.NAME_KEY, item.fileName)
                intent.putExtra(Params.URL_KEY, item.downloadUrl)
                context.startService(intent)
            }

            downloadingView?.onClickCancel = onCancel@ {
                val item = downloadItem ?: return@onCancel
                DownloadItemTransactionUtil.updateStatus(item, DownloadItem.DOWNLOAD_STATUS_FAILED)
                flipperLayout?.next(item.status)

                val intent = Intent(App.instance, DownloadService::class.java)
                intent.putExtra(Params.CANCELED_KEY, true)
                intent.putExtra(Params.URL_KEY, item.downloadUrl)
                context.startService(intent)
            }
        }

        internal fun bind(item: DownloadItem) {
            this.downloadItem = item

            draweeView?.let {
                var request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(item.thumbUrl))
                        .setResizeOptions(ResizeOptions(MAX_DIMENSION_PREVIEW_PX,
                                MAX_DIMENSION_PREVIEW_PX)).build()
                var controller = Fresco.newDraweeControllerBuilder().setImageRequest(request)
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
