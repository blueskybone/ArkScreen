package com.blueskybone.arkscreen.util

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import android.webkit.CookieManager
import androidx.core.content.ContextCompat.getString
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.R
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.hjq.toast.Toaster
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLDecoder

@SuppressLint("QueryPermissionsNeeded")
fun Context.launchApp(packageName: String, onAppNotFound: () -> Unit = {}) {
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        startActivity(intent)
    } else {
        onAppNotFound()
    }
}
fun getRealScreenSize(context: Context): Point {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = wm.currentWindowMetrics.bounds
        Point(
            bounds.width(),
            bounds.height()
        )
    } else {
        Point().apply {
            @Suppress("DEPRECATED")
            wm.defaultDisplay.getSize(this)
        }
    }
}

fun getJsonContent(jsonStr: String?, key: String): String {
    try {
        val om = ObjectMapper()
        val tree = om.readTree(jsonStr)
        val keys = tree.findValues(key)
        return keys[0].asText()
    } catch (e: Exception) {
        throw Exception("try get json content failed: content: $jsonStr , key: $key")
    }
}

fun readFileAsJsonNode(path: String): JsonNode {
    val inputStream = FileInputStream(path)
    val om = ObjectMapper()
    return om.readTree(inputStream)
}



fun dpToPx(context: Context, dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.resources.displayMetrics
    )
}

fun dpToPx(dp: Int): Int {
    return (dp * APP.resources.displayMetrics.density).toInt()
}

fun spToPx(context: Context, sp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp,
        context.resources.displayMetrics
    )
}

fun getColorFromAttr(context: Context, attr: Int): Int {
    val typedValue = TypedValue()
    val theme = context.theme
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data // 返回颜色值
}

fun getDensityDpi(context: Context): Int {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val displayMetrics = DisplayMetrics()
        context.resources.displayMetrics?.let {
            displayMetrics.setTo(it)
        }
        displayMetrics.densityDpi
    } else {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        metrics.densityDpi
    }
}

fun getScreenWidthDp(context: Context): Float {
    val displayMetrics = context.resources.displayMetrics
    val widthPixels = displayMetrics.widthPixels
    val density = displayMetrics.density
    return widthPixels / density
}

fun getScreenHeightDp(context: Context): Float {
    val displayMetrics = context.resources.displayMetrics
    val heightPixels = displayMetrics.heightPixels
    val density = displayMetrics.density
    return heightPixels / density
}

fun getScreenInfo(context: Context): String {
    val metrics = context.resources.displayMetrics
    val widthDp = metrics.widthPixels / metrics.density
    val heightDp = metrics.heightPixels / metrics.density
    val density = metrics.density
    val dpi = metrics.densityDpi

    return """
        屏幕信息:
        - 像素尺寸: ${metrics.widthPixels} × ${metrics.heightPixels} px
        - DP 尺寸: ${"%.1f".format(widthDp)} × ${"%.1f".format(heightDp)} dp
        - 屏幕密度: $density (比例)
        - DPI: $dpi
        - 密度级别: ${getDensityLevel(dpi)}
    """.trimIndent()
}

private fun getDensityLevel(dpi: Int): String {
    return when {
        dpi <= 120 -> "ldpi"
        dpi <= 160 -> "mdpi"
        dpi <= 240 -> "hdpi"
        dpi <= 320 -> "xhdpi"
        dpi <= 480 -> "xxhdpi"
        else -> "xxxhdpi"
    }
}

fun getAssetsFilepath(filename: String): String {
    val context = APP
    val filePath = "${context.externalCacheDir.toString()}/${filename}"
    val cacheFile = File(context.externalCacheDir, filename)
    return try {
        if (cacheFile.exists()) {
            filePath
        } else {
            try {
                context.assets.open(filename).use { inputStream ->
                    FileOutputStream(cacheFile).use { outputStream ->
                        val buf = ByteArray(4096)
                        var len: Int
                        while (inputStream.read(buf).also { len = it } > 0) {
                            outputStream.write(buf, 0, len)
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            cacheFile.absolutePath
        }
    } catch (e: Exception) {
        filePath
    }
}

fun getCookie(url: String, name: String): String? {
    val cookieManager = CookieManager.getInstance()
    val cookieStr = cookieManager.getCookie(url) ?: return null
    return cookieStr.split(";")
        .map { it.trim() }
        .firstOrNull { it.startsWith("$name=") }
        ?.substringAfter("=")
        ?.let {
            try {
                URLDecoder.decode(it, "UTF-8")
            } catch (e: Exception) {
                Timber.e(e, "Failed to decode cookie value")
                it // 返回未解码的原始值
            }
        }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("label", text)
    clipboardManager.setPrimaryClip(clipData)
    Toaster.show(getString(context, R.string.copied))
}
