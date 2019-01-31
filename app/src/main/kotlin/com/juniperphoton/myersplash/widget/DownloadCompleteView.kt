package com.juniperphoton.myersplash.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.activity.EditActivity
import com.juniperphoton.myersplash.extension.isLightColor
import java.io.File

@Suppress("unused")
class DownloadCompleteView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    @BindView(R.id.widget_set_as_rl)
    lateinit var setAsBtn: RelativeLayout

    @BindView(R.id.widget_set_as_root_rl)
    lateinit var setAsRL: RelativeLayout

    @BindView(R.id.set_as_tv)
    lateinit var setAsTextView: TextView

    var filePath: String? = null

    var themeColor: Int = Color.BLACK
        set(value) {
            field = value
            setAsRL.background = ColorDrawable(value)
            setAsTextView.setTextColor(if (value.isLightColor()) Color.BLACK else Color.WHITE)
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_download_complete_view, this)
        ButterKnife.bind(this)
    }

    @OnClick(R.id.widget_set_as_rl)
    fun setAs() {
        filePath?.let {
            val intent = Intent(context, EditActivity::class.java)
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(it)))
            context.startActivity(intent)
        }
    }
}
