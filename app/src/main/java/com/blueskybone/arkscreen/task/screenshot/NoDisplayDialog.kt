package com.blueskybone.arkscreen.task.screenshot

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import com.blueskybone.arkscreen.APP

/**
 *   Created by blueskybone
 *   Date: 2024/2/27
 */
class NoDisplayDialog(context: Context) : Dialog(context) {
    fun startScreenTask() {
        //Android14开始，不能通过保存ScreenshotPermission的方法反复截屏，
        //只能每次截屏获取一次动态权限。
        if (Build.VERSION.SDK_INT < 34 && APP.getScreenshotPermission() != null) {
            APP.startScreenshotRecruit()
        } else {
            val acquireIntent = Intent(context, AcquireScreenshotPermission::class.java)
            acquireIntent.putExtra(
                "SCREENSHOT_MODE",
                AcquireScreenshotPermission.SCREENSHOT_DEFAULT
            )
            acquireIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(acquireIntent)
        }
    }

    //just for float tile service
    fun prepareScreenTask() {
        if (APP.getScreenshotPermission() != null) return
        else {
            val acquireIntent = Intent(context, AcquireScreenshotPermission::class.java)
            acquireIntent.putExtra(
                "SCREENSHOT_MODE",
                AcquireScreenshotPermission.SCREENSHOT_PREPARE
            )
            acquireIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(acquireIntent)
        }
    }
}
