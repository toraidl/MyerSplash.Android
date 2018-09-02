package com.juniperphoton.myersplash.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.getVersionName
import com.juniperphoton.myersplash.extension.startActivitySafely

@Suppress("unused")
class AboutActivity : BaseActivity() {
    private val marginLeft by lazy {
        resources.getDimensionPixelSize(R.dimen.about_thanks_item_margin)
    }

    @BindView(R.id.version_text_view)
    lateinit var versionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)
        ButterKnife.bind(this)

        updateVersion()
    }

    private fun updateVersion() {
        versionTextView.text = getVersionName()
    }

    @OnClick(R.id.email_item)
    internal fun onClickEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_url)))

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MyerSplash for Android ${getVersionName()} feedback")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")

        startActivitySafely(intent)
    }

    @OnClick(R.id.rate_item)
    internal fun onClickRate() {
        val uri = Uri.parse("market://details?id=" + packageName)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivitySafely(intent)
    }

    @OnClick(R.id.github_item)
    internal fun onClickGitHub() {
        val uri = Uri.parse(getString(R.string.github_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivitySafely(intent)
    }

    @OnClick(R.id.twitter_item)
    internal fun onClickTwitter() {
        val uri = Uri.parse(getString(R.string.twitter_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivitySafely(intent)
    }

    @OnClick(R.id.weibo_item)
    internal fun onClickWeibo() {
        val uri = Uri.parse(getString(R.string.weibo_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivitySafely(intent)
    }
}