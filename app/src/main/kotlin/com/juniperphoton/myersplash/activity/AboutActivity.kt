package com.juniperphoton.myersplash.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.getVersionName
import com.juniperphoton.myersplash.extension.startActivitySafely
import kotlinx.android.synthetic.main.activity_about.*

@Suppress("unused")
class AboutActivity : BaseActivity() {
    private val marginLeft by lazy {
        resources.getDimensionPixelSize(R.dimen.about_thanks_item_margin)
    }

    private val idToUrl = mapOf(
            R.id.githubItem to R.string.github_url,
            R.id.weiboItem to R.string.weibo_url,
            R.id.twitterItem to R.string.twitter_url
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)

        updateVersion()

        emailItem.setOnClickListener(this)
        githubItem.setOnClickListener(this)
        weiboItem.setOnClickListener(this)
        twitterItem.setOnClickListener(this)
        rateItem.setOnClickListener(this)
    }

    override fun onClickView(v: View) {
        super.onClickView(v)
        when (v.id) {
            R.id.emailItem -> {
                onClickEmail()
            }
            R.id.githubItem,
            R.id.weiboItem,
            R.id.twitterItem -> {
                onClickUrl(getString(idToUrl.getValue(v.id)))
            }
            R.id.rateItem -> {
                onClickRate()
            }
        }
    }

    private fun updateVersion() {
        versionTextView.text = getVersionName()
    }

    private fun onClickEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_url)))

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MyerSplash for Android ${getVersionName()} feedback")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")

        startActivitySafely(Intent.createChooser(emailIntent, getString(R.string.email_title)))
    }

    private fun onClickUrl(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivitySafely(intent)
    }

    private fun onClickRate() {
        val uri = Uri.parse("market://details?id=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivitySafely(intent)
    }
}