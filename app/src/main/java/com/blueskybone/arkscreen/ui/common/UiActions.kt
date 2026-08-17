package com.blueskybone.arkscreen.ui.common

import android.content.Context
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.blueskybone.arkscreen.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator

fun View.setDebouncedClickListener(
    intervalMs: Long = 600L,
    action: (View) -> Unit,
) {
    setOnClickListener { view ->
        val now = System.currentTimeMillis()
        val lastClick = view.getTag(R.id.tag_last_click_time) as? Long ?: 0L
        if (now - lastClick >= intervalMs) {
            view.setTag(R.id.tag_last_click_time, now)
            action(view)
        }
    }
}

fun MenuItem.renderSyncing(context: Context, syncing: Boolean) {
    isEnabled = !syncing
    if (!syncing) {
        actionView = null
        setIcon(R.drawable.ic_refresh)
        return
    }
    actionView = FrameLayout(context).apply {
        minimumWidth = (48 * resources.displayMetrics.density).toInt()
        minimumHeight = (48 * resources.displayMetrics.density).toInt()
        addView(
            CircularProgressIndicator(context).apply {
                isIndeterminate = true
                indicatorSize = (22 * resources.displayMetrics.density).toInt()
                trackThickness = (2 * resources.displayMetrics.density).toInt()
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER,
            ),
        )
    }
}

fun Context.showDestructiveConfirmation(
    @StringRes titleRes: Int,
    message: CharSequence,
    @StringRes actionRes: Int = R.string.delete,
    onConfirm: () -> Unit,
) {
    val dialog = MaterialAlertDialogBuilder(this)
        .setTitle(titleRes)
        .setMessage(message)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(actionRes) { _, _ -> onConfirm() }
        .create()
    dialog.setOnShowListener {
        val button = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
        button.setTextColor(
            MaterialColors.getColor(button, com.google.android.material.R.attr.colorError)
        )
    }
    dialog.show()
}
