package com.juniperphoton.myersplash.cloudservice

import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadService {
    @Streaming
    @GET
    fun downloadFileAsync(@Url fileUrl: String): Deferred<ResponseBody>

    @GET
    fun reportDownloadAsync(@Url url: String): Deferred<ResponseBody>
}