package com.blueskybone.arkscreen.util

import android.graphics.Bitmap
import android.media.Image
import com.blueskybone.arkscreen.APP
import java.io.File
import java.io.FileOutputStream

/**
 *   Created by blueskybone
 *   Date: 2024/2/21
 */
fun convertImageToBitmap(image: Image, config: Bitmap.Config?): Bitmap {
    val width = image.width
    val height = image.height
    val bitmap: Bitmap
    val planes = image.planes
    val buffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * width
    bitmap = Bitmap.createBitmap(
        width + rowPadding / pixelStride /*equals: rowStride/pixelStride */, height, config!!
    )
    bitmap.copyPixelsFromBuffer(buffer)
    return Bitmap.createBitmap(bitmap, 0, 0, width, height)
}

/*
     * Capture the area in the middle only containing 5 tags.
     * These magic numbers are obtained through experiments based on the UI layout of Arknights.
     * */

fun getRoiBitmap(srcBitmap: Bitmap?, width: Int, height: Int): Bitmap {
    val x: Int
    val y: Int
    val roiWidth: Int
    val roiHeight: Int
    if (width > 2 * height) {
        y = (height / 2.06).toInt()
        roiHeight = (height / 5.143).toInt()
        x = (width / 2 - height / 2.572).toInt()
        roiWidth = (roiHeight * 4.0).toInt()
    } else {
        x = (width / 3.636).toInt()
        roiWidth = (width / 2.5).toInt()
        y = (height / 2 - width / 111.111).toInt()
        roiHeight = (roiWidth * 0.281).toInt()
    }
    return Bitmap.createBitmap(srcBitmap!!, x, y, roiWidth, roiHeight)
}

fun getScale(width: Int): Int {
    return width / 640
}

fun saveBitmap(bitmap: Bitmap, fileName: String) {
    val file = File(APP.externalCacheDir.toString(), fileName)
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    outputStream.flush()
    outputStream.close()
}