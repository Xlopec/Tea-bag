package com.max.weatherviewer.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.ui.graphics.Image
import androidx.ui.graphics.ImageConfig
import androidx.ui.graphics.NativeImage
import androidx.ui.graphics.colorspace.ColorSpace
import androidx.ui.graphics.colorspace.ColorSpaces
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.URL

suspend fun loadImage(url: URL, width: Int, height: Int): Bitmap =
    coroutineScope {

        withContext(Dispatchers.IO) {

            val options = url.openStream().use { input ->

                BitmapFactory.Options().apply {
                    inJustDecodeBounds = true

                    BitmapFactory.decodeStream(input, null, this)

                    inSampleSize =
                        calculateInSampleSize(
                            this,
                            width,
                            height
                        )

                    inJustDecodeBounds = false
                }
            }

            BitmapFactory.decodeStream(url.openStream(), null, options)
                ?: error("Couldn't load image $url")
        }
    }

fun loadImage2(url: URL, width: Int, height: Int, cb: (Bitmap) -> Unit){
    val options = url.openStream().use { input ->

        BitmapFactory.Options().apply {
            inJustDecodeBounds = true

            BitmapFactory.decodeStream(input, null, this)

            inSampleSize =
                calculateInSampleSize(
                    this,
                    width,
                    height
                )

            inJustDecodeBounds = false
        }
    }

    val bm = BitmapFactory.decodeStream(url.openStream(), null, options)
        ?: error("Couldn't load image $url")

    cb(bm)
}

class MyImage(val bitmap: Bitmap) : Image {

    /**
     * @see Image.width
     */
    override val width: Int
        get() = bitmap.width

    /**
     * @see Image.height
     */
    override val height: Int
        get() = bitmap.height

    override val config: ImageConfig
        get() = bitmap.config.toImageConfig()

    /**
     * @see Image.colorSpace
     */
    override val colorSpace: ColorSpace
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bitmap.colorSpace?.toComposeColorSpace() ?: ColorSpaces.Srgb
        } else {
            ColorSpaces.Srgb
        }

    /**
     * @see Image.hasAlpha
     */
    override val hasAlpha: Boolean
        get() = bitmap.hasAlpha()

    /**
     * @see Image.nativeImage
     */
    override val nativeImage: NativeImage
        get() = bitmap

    /**
     * @see
     */
    override fun prepareToDraw() {
        bitmap.prepareToDraw()
    }
}

private fun Bitmap.Config.toImageConfig(): ImageConfig {
    // Cannot utilize when statements with enums that may have different sets of supported
    // values between the compiled SDK and the platform version of the device.
    // As a workaround use if/else statements
    // See https://youtrack.jetbrains.com/issue/KT-30473 for details
    @Suppress("DEPRECATION")
    return if (this == Bitmap.Config.ALPHA_8) {
        ImageConfig.Alpha8
    } else if (this == Bitmap.Config.RGB_565) {
        ImageConfig.Rgb565
    } else if (this == Bitmap.Config.ARGB_4444) {
        ImageConfig.Argb8888 // Always upgrade to Argb_8888
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this == Bitmap.Config.RGBA_F16) {
        ImageConfig.F16
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this == Bitmap.Config.HARDWARE) {
        ImageConfig.Gpu
    } else {
        ImageConfig.Argb8888
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun android.graphics.ColorSpace.toComposeColorSpace(): ColorSpace {
    return when (this) {
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB)
        -> ColorSpaces.Srgb
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.ACES)
        -> ColorSpaces.Aces
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.ACESCG)
        -> ColorSpaces.Acescg
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.ADOBE_RGB)
        -> ColorSpaces.AdobeRgb
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.BT2020)
        -> ColorSpaces.Bt2020
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.BT709)
        -> ColorSpaces.Bt709
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.CIE_LAB)
        -> ColorSpaces.CieLab
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.CIE_XYZ)
        -> ColorSpaces.CieXyz
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.DCI_P3)
        -> ColorSpaces.DciP3
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.DISPLAY_P3)
        -> ColorSpaces.DisplayP3
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.EXTENDED_SRGB)
        -> ColorSpaces.ExtendedSrgb
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.LINEAR_EXTENDED_SRGB)
        -> ColorSpaces.LinearExtendedSrgb
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.LINEAR_SRGB)
        -> ColorSpaces.LinearSrgb
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.NTSC_1953)
        -> ColorSpaces.Ntsc1953
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.PRO_PHOTO_RGB)
        -> ColorSpaces.ProPhotoRgb
        android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SMPTE_C)
        -> ColorSpaces.SmpteC
        else -> ColorSpaces.Srgb
    }
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}