package com.blueskybone.arkscreen.ui.common

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import com.blueskybone.arkscreen.R

fun showChoicePopup(
    anchor: View,
    entries: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
): ListPopupWindow {
    val context = anchor.context
    fun dp(n: Int) = (n * context.resources.displayMetrics.density).toInt()
    val popup = ListPopupWindow(context)
    popup.anchorView = anchor
    popup.setDropDownGravity(Gravity.END)
    val labelPaint = TextView(context).apply { textSize = 16f }.paint
    val markPaint = TextView(context).apply { textSize = 18f }.paint
    val labelWidth = entries.maxOfOrNull { labelPaint.measureText(it) } ?: 0f
    val contentWidth = kotlin.math.ceil(labelWidth + markPaint.measureText("✓")).toInt() + dp(44)
    popup.width = minOf(contentWidth, context.resources.displayMetrics.widthPixels - dp(32))
    popup.isModal = true
    popup.setBackgroundDrawable(GradientDrawable().apply {
        cornerRadius = dp(20).toFloat()
        setColor(ContextCompat.getColor(context, R.color.app_surface))
    })
    popup.setAdapter(object : ArrayAdapter<String>(context, 0, entries) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                minimumHeight = dp(48)
                setPadding(dp(16), dp(8), dp(16), dp(8))
                addView(TextView(context).apply {
                    text = entries[position]
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.function_text_primary))
                }, LinearLayout.LayoutParams(0, -2, 1f))
                addView(TextView(context).apply {
                    text = if (position == selected) "✓" else ""
                    textSize = 18f
                    setPadding(dp(12), 0, 0, 0)
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    setTextColor(ContextCompat.getColor(context, R.color.function_text_primary))
                })
                isSelected = position == selected
            }
    })
    popup.setOnItemClickListener { _, _, position, _ ->
        popup.dismiss()
        onSelect(position)
    }
    popup.show()
    popup.listView?.apply {
        choiceMode = ListView.CHOICE_MODE_SINGLE
        if (selected in entries.indices) {
            setItemChecked(selected, true)
            setSelection(selected)
        }
    }
    return popup
}
