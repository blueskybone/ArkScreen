package com.blueskybone.arkscreen.platform.screenshot

class ScreenshotTaskFlowStore {

    private var pendingFlow: ScreenshotTaskFlow? = null

    fun set(flow: ScreenshotTaskFlow) {
        pendingFlow = flow
    }

    fun consume(): ScreenshotTaskFlow? {
        val flow = pendingFlow
        pendingFlow = null
        return flow
    }

    fun clear() {
        pendingFlow = null
    }
}