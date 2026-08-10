package com.blueskybone.arkscreen.ui.common.view

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import com.blueskybone.arkscreen.databinding.PreferenceBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Created by blueskybone
 * Date: 2026/1/30
 */
class PreferenceDialog(context: Context) : BottomSheetDialog(context) {
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

    fun add(title: Int, subTitle: Int, onClick: () -> Unit): PreferenceDialog {
        val item = PreferenceBinding.inflate(layoutInflater, linearLayout, true)
        item.Title.setText(title)
        item.Value.setText(subTitle)
        item.Icon.visibility = View.GONE
        item.root.setOnClickListener {
            dismiss()
            onClick()
        }
        return this
    }
}