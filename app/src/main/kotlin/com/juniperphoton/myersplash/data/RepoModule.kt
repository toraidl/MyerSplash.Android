package com.juniperphoton.myersplash.data

import android.content.Context
import com.juniperphoton.myersplash.model.UnsplashCategory
import dagger.Module
import dagger.Provides

@Module
class RepoModule(private val context: Context,
                 private val pos: Int,
                 private val view: MainContract.MainView) {
    @Provides
    fun providesCategory(): UnsplashCategory {
        return when (pos) {
            0 -> UnsplashCategory.newCategory
            1 -> UnsplashCategory.featuredCategory
            2 -> UnsplashCategory.highlightCategory
            else -> UnsplashCategory.searchCategory
        }
    }

    @Provides
    fun providesView(): MainContract.MainView = view

    @Provides
    fun providesPreferenceRepo(): PreferenceRepo = PreferenceRepo(context)
}