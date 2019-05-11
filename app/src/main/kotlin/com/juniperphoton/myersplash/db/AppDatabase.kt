package com.juniperphoton.myersplash.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.model.DownloadItem

@Database(entities = [DownloadItem::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE download_item ADD COLUMN width INTEGER, height INTEGER")
            }
        }

        val instance: AppDatabase by lazy {
            Room.databaseBuilder(
                    App.instance,
                    AppDatabase::class.java,
                    "myersplash_db"
            ).fallbackToDestructiveMigration().build()
        }
    }

    abstract fun downloadItemDao(): DownloadItemDao
}