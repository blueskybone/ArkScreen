package com.blueskybone.arkscreen.ui.common

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.content.res.AppCompatResources
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
    popup.setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.popup_bg))
    popup.setAdapter(object : ArrayAdapter<String>(context, 0, entries) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_choice_popup, parent, false)
            row.findViewById<TextView>(R.id.ChoiceLabel).text = entries[position]
            row.findViewById<TextView>(R.id.ChoiceMark).text =
                if (position == selected) "✓" else ""
            row.isSelected = position == selected
            return row
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
