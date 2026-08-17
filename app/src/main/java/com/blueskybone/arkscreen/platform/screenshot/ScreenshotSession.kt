package com.blueskybone.arkscreen.platform.screenshot

import android.content.Intent

class ScreenshotSession {

    private var resultCode: Int? = null
    private var data: Intent? = null

    fun save(resultCode: Int, data: Intent) {
        this.resultCode = resultCode
        this.data = data
    }

    fun clear() {
        resultCode = null
        data = null
    }

    fun getResultCode(): Int? = resultCode

    fun getData(): Intent? = data

    fun hasPermission(): Boolean {
        return resultCode != null && data != null
    }
}