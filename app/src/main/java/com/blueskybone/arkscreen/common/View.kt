package com.blueskybone.arkscreen.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.marginStart
import androidx.core.view.setMargins
import androidx.core.view.updateMargins
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.base.data.Operator
import com.blueskybone.arkscreen.task.recruit.RecruitManager
import com.blueskybone.arkscreen.util.dpToPx
import kotlinx.coroutines.withContext

/**
 *   Created by blueskybone
 *   Date: 2024/8/1
 */
fun tagTextView(context: Context, text: String): TextView {
    val textView = TextView(context)
    textView.setPadding(4, 2, 4, 2)
    val shapeDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 2f
        setColor(ContextCompat.getColor(context, R.color.blue_500))
    }
    textView.background = shapeDrawable
    textView.gravity = Gravity.CENTER
    textView.text = text
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    textView.setTextColor(ContextCompat.getColor(context, R.color.white))
    textView.visibility = View.VISIBLE
    textView.layoutParams = LayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT
    )
    return textView
}


fun tagButtonView(context: Context, text: String): Button {
    val button = Button(context)
    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
    )
    layoutParams.setMargins(0, 0, dpToPx(context, 5F).toInt(), dpToPx(context, 5F).toInt())
    button.layoutParams = layoutParams
    button.text = text
    button.minWidth = 0
    button.minHeight = 0
    button.minimumWidth = 0
    button.minimumHeight = 0
    button.setPadding(
        dpToPx(context, 10F).toInt(),
        dpToPx(context, 5F).toInt(),
        dpToPx(context, 10F).toInt(),
        dpToPx(context, 5F).toInt()
    )
    button.isEnabled = true
    button.isSelected = false
    button.isClickable = true
    button.stateListAnimator = null
    //ViewCompat.setElevation(button, 0F)
    return button
}


fun opeTextView(context: Context, ope: RecruitManager.Operator): TextView {
    val textView = tagTextView(context, ope.name)
    val colorId = getRarityColorId(ope.rare)
    val shapeDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 2f
        setColor(ContextCompat.getColor(context, colorId))
    }
    textView.background = shapeDrawable
    return textView
}

fun createLine(context: Context): View {
    val line = View(context)
    val params = ViewGroup.MarginLayoutParams(LayoutParams.MATCH_PARENT, 5)
    params.updateMargins(0, 15, 0, 15)
    line.layoutParams = params
    line.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_200))
    return line
}

fun createSpace(context: Context): Space {
    val space = Space(context)
    val spaceLayoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        10
    )
    space.layoutParams = spaceLayoutParams
    return space
}

fun getRarityColorId(rarity: Int): Int {
    return when (rarity) {
        6 -> R.color.rare_6
        5 -> R.color.rare_5
        4 -> R.color.rare_4
        3 -> R.color.rare_3
        2 -> R.color.rare_2
        else -> R.color.rare_1
    }
}

