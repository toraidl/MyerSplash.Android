package com.juniperphoton.myersplash.api

import com.juniperphoton.myersplash.utils.AnalysisHelper
import okhttp3.OkHttpClient

object OkHttpClientAPI {
    fun createClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
                .addNetworkInterceptor { chain ->
                    val startMs = System.currentTimeMillis()
                    val request = chain.request()

                    try {
                        val response = chain.proceed(request)
                        val durationMs = System.currentTimeMillis() - startMs
                        AnalysisHelper.logDownloadSuccess(durationMs)
                        return@addNetworkInterceptor response
                    } catch (e: Exception) {
                        val durationMs = System.currentTimeMillis() - startMs
                        AnalysisHelper.logDownloadFailed(e, durationMs)
                        throw e
                    }
                }
        return builder.build()
    }
}