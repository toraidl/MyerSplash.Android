package com.juniperphoton.myersplash.widget.item

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.juniperphoton.myersplash.R

class PhotoFooterView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    @BindView(R.id.footer_hint)
    lateinit var footerHint: TextView

    @BindView(R.id.progress_view)
    lateinit var progressView: View

    override fun onFinishInflate() {
        super.onFinishInflate()
        ButterKnife.bind(this, this)
    }

    fun toggleCollapsed() {
        visibility = View.GONE
    }

    fun toggleLoading() {
        visibility = View.VISIBLE
        progressView.visibility = View.VISIBLE
        footerHint.text = context.getString(R.string.loading)
    }

    fun indicateEnd() {
        visibility = View.VISIBLE
        progressView.visibility = View.GONE
        footerHint.text = context.getString(R.string.end)
    }
}