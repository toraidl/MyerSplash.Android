package com.juniperphoton.myersplash.contract

import android.content.Context
import com.juniperphoton.myersplash.model.UnsplashCategory
import dagger.Module
import dagger.Provides

@Module
class RepoModule(private val context: Context,
                 private val categoryId: Int,
                 private val view: MainContract.MainView) {
    @Provides
    fun providesCategory(): UnsplashCategory {
        return when (categoryId) {
            UnsplashCategory.NEW_CATEGORY_ID -> UnsplashCategory.newCategory
            UnsplashCategory.FEATURED_CATEGORY_ID -> UnsplashCategory.featuredCategory
            UnsplashCategory.HIGHLIGHTS_CATEGORY_ID -> UnsplashCategory.highlightCategory
            UnsplashCategory.RANDOM_CATEGORY_ID -> UnsplashCategory.randomCategory
            else -> UnsplashCategory.searchCategory
        }
    }

    @Provides
    fun providesView(): MainContract.MainView = view

    @Provides
    fun providesPreferenceRepo(): PreferenceRepo = PreferenceRepo(context)
}