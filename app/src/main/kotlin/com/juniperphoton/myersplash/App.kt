package com.juniperphoton.myersplash

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.juniperphoton.myersplash.utils.Pasteur
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Pasteur.init(BuildConfig.DEBUG)
        Fresco.initialize(this)
        RealmCache.init(this)
        AppCenter.start(this, BuildConfig.APP_CENTER_KEY, Analytics::class.java, Crashes::class.java)
    }
}