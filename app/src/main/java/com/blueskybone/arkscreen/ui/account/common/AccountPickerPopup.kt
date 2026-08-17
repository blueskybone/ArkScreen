package com.blueskybone.arkscreen.ui.account.common

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.widget.PopupWindowCompat
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.databinding.PopupAccountBinding
import com.blueskybone.arkscreen.ui.account.adapter.AccountAdapter

class AccountPickerPopup(
    private val activity: Activity,
    adapter: AccountAdapter,
) {
    private val density = activity.resources.displayMetrics.density
    private val screenWidth = activity.resources.displayMetrics.widthPixels
    private val binding = PopupAccountBinding.inflate(LayoutInflater.from(activity))
    private val popup = PopupWindow(
        binding.root,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        true,
    ).apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isOutsideTouchable = true
        isClippingEnabled = true
        elevation = 8 * density
        animationStyle = R.style.PopupDownAnim
        setOnDismissListener {
            activity.window.attributes = activity.window.attributes.apply {
                alpha = 1F
            }
        }
    }

    val isShowing: Boolean
        get() = popup.isShowing

    init {
        binding.lvAccount.adapter = adapter
    }

    fun show(anchor: View) {
        val fixedWidth = (280 * density).toInt()
        val horizontalMargin = (16 * density).toInt()
        popup.width = fixedWidth.coerceAtMost(screenWidth - horizontalMargin * 2)
        activity.window.attributes = activity.window.attributes.apply {
            alpha = 0.7F
        }
        PopupWindowCompat.showAsDropDown(
            popup,
            anchor,
            0,
            (4 * density).toInt(),
            Gravity.START,
        )
    }

    fun dismiss() {
        popup.dismiss()
    }
}
