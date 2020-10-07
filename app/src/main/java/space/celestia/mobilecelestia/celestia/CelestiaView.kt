/*
 * CelestiaView.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.celestia

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import android.opengl.GLSurfaceView
import android.view.Choreographer
import space.celestia.mobilecelestia.core.CelestiaAppCore

@SuppressLint("ViewConstructor")
class CelestiaView(context: Context, val scaleFactor: Float) : GLSurfaceView(context), Choreographer.FrameCallback {
    interface Listener {
        fun willDrawFrame(view: CelestiaView)
    }

    var isReady = false
    var listener: Listener? = null

    override fun finalize() {
        Choreographer.getInstance().removeFrameCallback(this)

        super.finalize()
    }

    private val core by lazy { CelestiaAppCore.shared() }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        holder.setFixedSize((width * scaleFactor).toInt(), (height * scaleFactor).toInt())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        sharedView = this
    }

    override fun onDetachedFromWindow() {
        sharedView = null
        listener = null

        super.onDetachedFromWindow()
    }

    override fun doFrame(p0: Long) {
        if (isReady) {
            requestRender()
        }

        listener?.willDrawFrame(this)
        Choreographer.getInstance().postFrameCallback(this)
    }

    init {
        Choreographer.getInstance().postFrameCallback(this)
    }

    companion object {
        private const val TAG = "CelestiaView"
        private var sharedView: CelestiaView? = null

        // Call on render thread to avoid concurrency issue.
        fun callOnRenderThread(block: () -> Unit) {
            sharedView?.queueEvent(block)
        }
    }
}

fun PointF.scaleBy(factor: Float): PointF {
    return PointF(x * factor, y * factor)
}

fun RectF.scaleBy(factor: Float): RectF {
    return RectF(left * factor, top * factor, right * factor, bottom * factor)
}
