package com.juniperphoton.myersplash.model

import com.juniperphoton.myersplash.api.Request

@Suppress("unused")
class UnsplashCategory {
    companion object {
        const val NEW_CATEGORY_ID = 10000
        const val FEATURED_CATEGORY_ID = 10001
        const val HIGHLIGHTS_CATEGORY_ID = 10002
        const val SEARCH_ID = 10003
        const val RANDOM_CATEGORY_ID = 10004

        private const val FEATURE = "Featured"
        private const val NEW = "New"
        private const val HIGHLIGHTS = "Highlights"

        val featuredCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = FEATURED_CATEGORY_ID
                    title = FEATURE
                }
            }

        val newCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = NEW_CATEGORY_ID
                    title = NEW
                }
            }

        val highlightCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = HIGHLIGHTS_CATEGORY_ID
                    title = HIGHLIGHTS
                }
            }

        val searchCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = SEARCH_ID
                }
            }

        val randomCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = RANDOM_CATEGORY_ID
                }
            }
    }

    private val photoCount: Int = 0
    private val links: Links? = null

    var id: Int = 0
    var title: String? = null

    val requestUrl: String?
        get() = when (id) {
            NEW_CATEGORY_ID -> Request.PHOTO_URL
            FEATURED_CATEGORY_ID -> Request.FEATURED_PHOTO_URL
            HIGHLIGHTS_CATEGORY_ID -> Request.HIGHLIGHTS_PHOTO_URL
            RANDOM_CATEGORY_ID -> Request.RANDOM_PHOTOS_URL
            SEARCH_ID -> Request.SEARCH_URL
            else -> links?.photos
        }

    val websiteUrl: String?
        get() = links?.html
}

@Suppress("unused")
class Links {
    val self: String? = null
    val photos: String? = null
    val html: String? = null
}