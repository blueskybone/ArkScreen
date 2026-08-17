package com.blueskybone.arkscreen.ui.main

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat

fun saveDrawableToGallery(context: Context, drawableId: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableId) as? BitmapDrawable ?: return
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "Image_${System.currentTimeMillis()}.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }
    val resolver = context.contentResolver
    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
        resolver.openOutputStream(uri)?.use { output ->
            drawable.bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }
}
