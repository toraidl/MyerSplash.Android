package com.juniperphoton.myersplash.extension

import android.graphics.BitmapFactory
import androidx.palette.graphics.Palette
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val IN_SAMPLE_SIZE = 4

suspend fun UnsplashImage.extractThemeColor(): Int = withContext(Dispatchers.IO) {
    val file = FileUtils.getCachedFile(listUrl!!) ?: kotlin.run {
        return@withContext Int.MIN_VALUE
    }

    val o = BitmapFactory.Options()
    o.inSampleSize = IN_SAMPLE_SIZE

    val bm = BitmapFactory.decodeFile(file.absolutePath, o)

    return@withContext Palette.from(bm).generate().darkVibrantSwatch?.rgb ?: Int.MIN_VALUE
}