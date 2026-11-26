package com.blueskybone.arkscreen.platform.tile

import android.view.ViewTreeObserver
import com.hjq.window.EasyWindow
import com.hjq.window.draggable.MovingDraggable

/** Do not rotate EasyWindow's zero-initialized coordinates before its first layout. */
internal class InitialLayoutAwareDraggable : MovingDraggable() {
    private var positionReady = false

    override fun start(easyWindow: EasyWindow<*>) {
        positionReady = false
        super.start(easyWindow)
        val view = decorView
        view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (view.width <= 0 || view.height <= 0) return true
                refreshWindowInfo()
                refreshLocationCoordinate()
                if (windowWidth <= 0 || windowHeight <= 0) return true
                positionReady = true
                view.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    override fun onScreenOrientationChange() {
        // Initial configuration delivery is not a rotation of an established position.
        // Let WindowManager place the window; pre-draw then captures valid coordinates.
        if (!positionReady) return
        super.onScreenOrientationChange()
    }
}
