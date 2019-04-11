package com.juniperphoton.myersplash.cloudservice

import com.juniperphoton.myersplash.model.SearchResult
import com.juniperphoton.myersplash.model.UnsplashFeaturedImage
import com.juniperphoton.myersplash.model.UnsplashImage
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface PhotoService {
    @GET
    fun getPhotos(@Url url: String,
                  @Query("page") page: Int,
                  @Query("per_page") per_page: Int): Deferred<MutableList<UnsplashImage>>

    @GET
    fun getFeaturedPhotos(@Url url: String,
                          @Query("page") page: Int,
                          @Query("per_page") per_page: Int): Deferred<MutableList<UnsplashFeaturedImage>>

    @GET
    fun searchPhotosByQuery(@Url url: String,
                            @Query("page") page: Int,
                            @Query("per_page") per_page: Int,
                            @Query("query") query: String): Deferred<SearchResult>
}
