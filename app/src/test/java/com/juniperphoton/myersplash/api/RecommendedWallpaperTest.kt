package com.juniperphoton.myersplash.api

import com.juniperphoton.myersplash.cloudservice.CloudService
import com.juniperphoton.myersplash.cloudservice.Request
import com.juniperphoton.myersplash.model.UnsplashImageFactory
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
    fun testRecommendedThumb() {
        CloudService.downloadPhoto(thumbUrl).test().assertComplete()
    }

    @Test
    fun testCantDownloadRecommendedThumb() {
        CloudService.downloadPhoto(invalidUrl).test().assertEmpty()
    }

    @Test
    fun testRecommendedLarge() {
        CloudService.downloadPhoto(largeUrl).test().assertComplete()
    }
}