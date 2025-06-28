package com.blueskybone.arkscreen.ui.common

import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import com.blueskybone.arkscreen.databinding.MenuItemBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 *   Created by blueskybone
 *   Date: 2025/1/22
 */

class MenuDialog(context: Context) : BottomSheetDialog(context) {

    private val linearLayout: LinearLayout

    init {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        linearLayout = LinearLayout(context)
        linearLayout.layoutParams = params
        linearLayout.orientation = LinearLayout.VERTICAL

        val scrollView = NestedScrollView(context)
        scrollView.layoutParams = params

        scrollView.addView(linearLayout)
        setContentView(scrollView)
    }

    fun add(title: Int, onClick: () -> Unit): MenuDialog {
        val item = MenuItemBinding.inflate(layoutInflater, linearLayout, true).root
        item.setText(title)
        item.setOnClickListener {
            dismiss()
            onClick()
        }
        return this
    }

    fun add(title: String, onClick: () -> Unit): MenuDialog {
        val item = MenuItemBinding.inflate(layoutInflater, linearLayout, true).root
        item.text = title
        item.setOnClickListener {
            dismiss()
            onClick()
        }
        return this
    }
}