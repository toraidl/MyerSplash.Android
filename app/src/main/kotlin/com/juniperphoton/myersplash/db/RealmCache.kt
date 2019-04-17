package com.juniperphoton.myersplash.db

import android.content.Context
import io.realm.DynamicRealm
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmMigration

object RealmCache {
    private const val SCHEMA_VERSION = 3L
    private var configuration: RealmConfiguration? = null

    fun init(context: Context) {
        Realm.init(context)
        configuration = RealmConfiguration.Builder()
                .schemaVersion(SCHEMA_VERSION)
                .migration(CacheMigration())
                .build()
    }

    fun getInstance() = Realm.getInstance(configuration)!!
}

@Suppress("UNUSED_CHANGED_VALUE")
class CacheMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {
        val schema = realm!!.schema
        var oldVer = oldVersion
        if (oldVer < 2L) {
            schema.get("DownloadItem").renameField("mThumbUrl", "thumbUrl")
                    .renameField("mDownloadUrl", "downloadUrl")
                    .renameField("mId", "id")
                    .renameField("mProgress", "progress")
                    .renameField("mColor", "color")
                    .renameField("mStatus", "status")
                    .renameField("mFilePath", "filePath")
                    .renameField("mFileName", "fileName")
            oldVer++
        }
        if (oldVer == 2L) {
            schema.get("DownloadItem").addField("position", Int::class.java)
            oldVer++
        }
    }
}