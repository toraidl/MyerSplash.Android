package com.juniperphoton.myersplash.utils

import android.graphics.BitmapFactory
import androidx.palette.graphics.Palette
import com.juniperphoton.myersplash.model.UnsplashImage
import io.reactivex.Maybe

object PaletteUtil {
    private const val IN_SAMPLE_SIZE = 4

    fun extractThemeColorFromUnsplashImage(image: UnsplashImage?): Maybe<Int> {
        return Maybe.create { emitter ->
            image ?: kotlin.run {
                emitter.onComplete()
                return@create
            }

            val file = FileUtil.getCachedFile(image.listUrl!!) ?: kotlin.run {
                emitter.onComplete()
                return@create
            }

            val o = BitmapFactory.Options()
            o.inSampleSize = IN_SAMPLE_SIZE

            val bm = BitmapFactory.decodeFile(file.absolutePath, o)

            val color = Palette.from(bm).generate().darkVibrantSwatch?.rgb ?: kotlin.run {
                emitter.onComplete()
                return@create
            }

            emitter.onSuccess(color)
        }
    }
}