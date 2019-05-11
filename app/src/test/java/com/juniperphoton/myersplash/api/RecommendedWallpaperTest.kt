package com.juniperphoton.myersplash.api

import com.juniperphoton.myersplash.model.UnsplashImageFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.Test
import java.util.*

class RecommendedWallpaperTest {
    private val thumbUrl: String
        get() {
            return "${Request.AUTO_CHANGE_WALLPAPER_THUMB}${UnsplashImageFactory.createDateString(Date())}.jpg"
        }

    private val largeUrl: String
        get() {
            return "${Request.AUTO_CHANGE_WALLPAPER}${UnsplashImageFactory.createDateString(Date())}.jpg"
        }

    private val invalidUrl: String
        get() {
            return "${Request.AUTO_CHANGE_WALLPAPER_THUMB}nothumb.jpg"
        }

    @Test
    suspend fun testRecommendedThumb() {
        withContext(Dispatchers.IO) {
            CloudService.downloadPhoto(thumbUrl)
        }
    }

    @Test
    suspend fun testCantDownloadRecommendedThumb() {
        withContext(Dispatchers.IO) {
            CloudService.downloadPhoto(invalidUrl)
        }
    }

    @Test
    suspend fun testRecommendedLarge() {
        withContext(Dispatchers.IO) {
            CloudService.downloadPhoto(largeUrl)
        }
    }
}