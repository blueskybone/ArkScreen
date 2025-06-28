package com.blueskybone.arkscreen.common

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.util.dpToPx

/**
 *   Created by blueskybone
 *   Date: 2025/1/22
 */

fun line(context: Context): View {
    val line = View(context)
    val params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2)
    params.updateMargins(0, 0, 0, 15)
    line.layoutParams = params
    line.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_500))
    return line
}

fun space(context:Context, dp:Float): Space {
    val px = dpToPx(context, dp)
    return Space(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            px.toInt()
        )
    }
}