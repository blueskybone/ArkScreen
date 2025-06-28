package com.blueskybone.arkscreen.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.load
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.activity.WebViewActivity
import com.blueskybone.arkscreen.util.dpToPx
import com.hjq.toast.Toaster

/**
 *   Created by blueskybone
 *   Date: 2025/6/15
 */

fun linkRound(context: Context, title: String, url: String,icon: String?=null, position: Int): View {
    val linearLayout = LinearLayout(context).apply {
        layoutParams = GridLayout.LayoutParams().apply {
            val row = position / 4
            val column = position % 4
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // UNDEFINED 表示自动分配列
//            rowSpec = GridLayout.spec(row)
            columnSpec = GridLayout.spec(column)
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
        }
        orientation = LinearLayout.VERTICAL
        gravity = android.view.Gravity.CENTER
        isClickable = true
        isFocusable = true
    }

    // 2. 创建 ImageView（设置 padding 和默认 contentDescription）
    val imageView = ImageView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            40.dpToPx(context),40.dpToPx(context)
        )
        setPadding(5.dpToPx(context), 5.dpToPx(context), 5.dpToPx(context), 5.dpToPx(context)) // 确保 strings.xml 中有定义
    }

    // 3. 创建 TextView（设置 padding 和 textSize）
    val textView = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        setPadding(5.dpToPx(context), 5.dpToPx(context), 5.dpToPx(context), 5.dpToPx(context))
        textSize = 12f
        text = title
    }

    // 4. 将 ImageView 和 TextView 添加到 LinearLayout
    linearLayout.addView(imageView)
    linearLayout.addView(textView)

    // 5. 如果有 URL，可以加载图片（例如使用 Glide/Coil）
    if (icon == null) {
        // 加载默认图片（假设 R.drawable.ic_default 是默认图标）
        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_default))
    } else {
        // 使用 Coil 加载网络图片
        imageView.load(icon) {
            placeholder( R.drawable.ic_default)  // 加载中的占位图
            error( R.drawable.ic_default)               // 加载失败的占位图
            crossfade(true)                         // 淡入淡出效果
        }
    }
    return linearLayout
}

fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}