package com.juniperphoton.myersplash.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.model.DownloadItemDao

@Database(entities = [DownloadItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        val instance: AppDatabase by lazy {
            Room.databaseBuilder(
                    App.instance,
                    AppDatabase::class.java,
                    "myersplash_db"
            ).build()
        }
    }

    abstract fun downloadItemDao(): DownloadItemDao
}