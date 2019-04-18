package com.juniperphoton.myersplash.api

import com.juniperphoton.myersplash.model.SearchResult
import com.juniperphoton.myersplash.model.UnsplashFeaturedImage
import com.juniperphoton.myersplash.model.UnsplashImage
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface PhotoService {
    @GET
    fun getPhotosAsync(@Url url: String,
                       @Query("page") page: Int,
                       @Query("per_page") per_page: Int): Deferred<MutableList<UnsplashImage>>

    @GET
    fun getFeaturedPhotosAsync(@Url url: String,
                               @Query("page") page: Int,
                               @Query("per_page") per_page: Int): Deferred<MutableList<UnsplashFeaturedImage>>

    @GET
    fun searchPhotosByQueryAsync(@Url url: String,
                                 @Query("page") page: Int,
                                 @Query("per_page") per_page: Int,
                                 @Query("query") query: String): Deferred<SearchResult>
}
