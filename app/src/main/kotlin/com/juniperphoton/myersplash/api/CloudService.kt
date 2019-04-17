package com.juniperphoton.myersplash.api

import android.annotation.SuppressLint
import android.util.Log
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.juniperphoton.myersplash.BuildConfig
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.model.UnsplashImageFactory
import com.juniperphoton.myersplash.presenter.MainListPresenter.Companion.DEFAULT_PAGING
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Suppress("DEPRECATION")
object CloudService {
    private const val DEFAULT_TIMEOUT = 10
    private const val DEFAULT_REQUEST_COUNT = 10
    private const val DEFAULT_HIGHLIGHTS_COUNT = 60

    private const val DOWNLOAD_TIMEOUT_MS = 30_000L
    private const val HIGHLIGHTS_DELAY_MS = 200L

    private const val TAG = "CloudService"

    private val endDate = SimpleDateFormat("yyyy/MM/dd").parse("2017/03/20")

    private val retrofit: Retrofit
    private val photoService: PhotoService
    private val ioService: IOService
    private val builder: OkHttpClient.Builder = OkHttpClient.Builder()

    init {
        if (BuildConfig.DEBUG) {
            val ctx = SSLContext.getInstance("SSL")

            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            })

            ctx.init(null, trustAllCerts, SecureRandom())

            builder.sslSocketFactory(ctx.socketFactory)
        }

        builder.connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .addInterceptor(CustomInterceptor())

        retrofit = Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .baseUrl(Request.BASE_URL)
                .build()

        photoService = retrofit.create(PhotoService::class.java)
        ioService = retrofit.create(IOService::class.java)
    }

    suspend fun getPhotos(url: String,
                          page: Int): MutableList<UnsplashImage> {
        val list = photoService.getPhotosAsync(url, page, DEFAULT_REQUEST_COUNT).await()
        if (page == DEFAULT_PAGING) {
            list.add(0, UnsplashImageFactory.createTodayImage())
        }
        return list
    }

    suspend fun getFeaturedPhotos(url: String,
                                  page: Int): MutableList<UnsplashImage> {
        val mutableList = mutableListOf<UnsplashImage>()
        return photoService
                .getFeaturedPhotosAsync(url, page, DEFAULT_REQUEST_COUNT).await().mapTo(mutableList) {
                    it.image!!
                }
    }

    suspend fun getHighlightsPhotos(page: Int): MutableList<UnsplashImage> {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.add(Calendar.DATE, -(page - 1) * DEFAULT_HIGHLIGHTS_COUNT)

        val list = mutableListOf<UnsplashImage>()

        for (i in 0 until DEFAULT_HIGHLIGHTS_COUNT) {
            val date = calendar.time
            if (date > endDate) {
                list.add(UnsplashImageFactory.createHighlightImage(calendar.time))
            } else {
                Log.w(TAG, "the date: $date is before end date $endDate")
            }
            calendar.add(Calendar.DATE, -1)
        }

        delay(HIGHLIGHTS_DELAY_MS)

        return list
    }

    suspend fun searchPhotos(url: String,
                             page: Int,
                             query: String): MutableList<UnsplashImage> {
        return photoService
                .searchPhotosByQueryAsync(url, page, DEFAULT_REQUEST_COUNT, query).await().list!!
    }

    suspend fun downloadPhoto(url: String): ResponseBody {
        return withTimeout(DOWNLOAD_TIMEOUT_MS) {
            ioService.downloadFileAsync(url).await()
        }
    }

    suspend fun reportDownload(url: String): ResponseBody {
        return ioService.reportDownloadAsync(url).await()
    }
}
