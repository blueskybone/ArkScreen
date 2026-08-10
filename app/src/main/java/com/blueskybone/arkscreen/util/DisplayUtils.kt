package com.blueskybone.arkscreen.util

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import com.blueskybone.arkscreen.APP

fun getRealScreenSize(context: Context): Point {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = windowManager.currentWindowMetrics.bounds
        Point(bounds.width(), bounds.height())
    } else {
        Point().apply {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getSize(this)
        }
    }
}

fun dpToPx(context: Context, dp: Float): Float =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.resources.displayMetrics,
    )

fun dpToPx(dp: Int): Int =
    (dp * APP.resources.displayMetrics.density).toInt()

fun getDensityDpi(context: Context): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return context.resources.displayMetrics.densityDpi
    }
    val metrics = DisplayMetrics()
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    @Suppress("DEPRECATION")
    windowManager.defaultDisplay.getRealMetrics(metrics)
    return metrics.densityDpi
}

fun getScreenWidthDp(context: Context): Float =
    context.resources.displayMetrics.run { widthPixels / density }

fun getScreenHeightDp(context: Context): Float =
    context.resources.displayMetrics.run { heightPixels / density }
