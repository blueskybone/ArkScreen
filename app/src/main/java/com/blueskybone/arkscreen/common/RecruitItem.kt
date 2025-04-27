package com.blueskybone.arkscreen.common

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import com.blueskybone.arkscreen.R

/**
 *   Created by blueskybone
 *   Date: 2025/1/22
 */

fun line(context: Context): View {
    val line = View(context)
    val params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5)
    params.updateMargins(0, 15, 0, 15)
    line.layoutParams = params
    line.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_200))
    return line
}