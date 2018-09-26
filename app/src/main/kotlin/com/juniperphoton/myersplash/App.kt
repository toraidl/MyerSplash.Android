package com.juniperphoton.myersplash

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.juniperphoton.myersplash.utils.Pasteur

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
    }
}