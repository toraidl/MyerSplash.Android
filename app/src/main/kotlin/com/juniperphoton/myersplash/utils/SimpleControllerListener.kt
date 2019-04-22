package com.juniperphoton.myersplash.utils

import android.graphics.drawable.Animatable
import android.widget.SeekBar
import com.facebook.drawee.controller.ControllerListener
import com.facebook.imagepipeline.image.ImageInfo

open class SimpleControllerListener : ControllerListener<ImageInfo> {
    override fun onIntermediateImageFailed(id: String?, throwable: Throwable?) = Unit
    override fun onRelease(id: String?) = Unit
    override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) = Unit
    override fun onSubmit(id: String?, callerContext: Any?) = Unit
    override fun onFailure(id: String?, throwable: Throwable?) = Unit
    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) = Unit
}

open class SimpleOnSeekBarChangeListener : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) = Unit
    override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
    override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
}