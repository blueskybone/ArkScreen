package com.blueskybone.arkscreen.task.screenshot

import android.app.Activity
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 *   Created by blueskybone
 *   Date: 2025/1/25
 */
class TransActivity : Activity() {

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    companion object {
        var instance: TransActivity? = null

        private var activityRef: WeakReference<TransActivity>? = null
        fun finishActivity() {
            if (activityRef != null && activityRef!!.get() != null) {
                activityRef!!.get()!!.finish()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        instance = this
        super.onCreate(savedInstanceState)
        activityRef = WeakReference(this)
        finish()
    }
}