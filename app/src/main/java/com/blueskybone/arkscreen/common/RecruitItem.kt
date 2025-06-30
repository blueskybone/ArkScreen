package com.blueskybone.arkscreen.common

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.util.dpToPx
import com.nex3z.flowlayout.FlowLayout

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



fun Button.setTagLayout(text: String) {
    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
    )
    layoutParams.setMargins(0, 0, dpToPx(context, 5F).toInt(), dpToPx(context, 5F).toInt())
    this.layoutParams = layoutParams
    this.text = text
    this.minWidth = 0
    this.minHeight = 0
    this.minimumWidth = 0
    this.minimumHeight = 0
    this.setPadding(
        dpToPx(context, 10F).toInt(),
        dpToPx(context, 5F).toInt(),
        dpToPx(context, 10F).toInt(),
        dpToPx(context, 5F).toInt()
    )
    this.isEnabled = true
    this.isSelected = false
    this.isClickable = true
    this.stateListAnimator = null
    this.setTextColor(Color.WHITE)
}

fun getFlowLayout(context: Context): FlowLayout {
    val flowLayout = FlowLayout(context)
    flowLayout.childSpacingForLastRow = FlowLayout.SPACING_ALIGN
    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
    )
    flowLayout.layoutParams = layoutParams
    return flowLayout
}