/*
 * CelestiaInteraction.kt
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
import android.content.res.Resources
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.os.Build
import android.util.Log
import android.view.*
import space.celestia.mobilecelestia.core.CelestiaAppCore

open class CelestiaBaseInteraction(context: Context): View.OnTouchListener, View.OnKeyListener, View.OnFocusChangeListener, ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {
    protected val core by lazy { CelestiaAppCore.shared() }

    enum class InteractionMode {
        Object, Camera;

        val button: Int
            get() = when (this) {
                Camera -> CelestiaAppCore.MOUSE_BUTTON_LEFT
                Object -> CelestiaAppCore.MOUSE_BUTTON_RIGHT
            }

        val next: InteractionMode
            get() = when (this) {
                Camera -> Object
                Object -> Camera
            }
    }

    enum class ZoomMode {
        In, Out;

        val distance: Float
            get() = when (this) {
                In -> -1.5f
                Out -> 1.5f
            }
    }

    interface Listener {
        fun showContextMenu(celestiaLocation: PointF, viewLocation: PointF)
    }

    private val scaleGestureDetector = ScaleGestureDetector(context, this)
    protected val gestureDetector = GestureDetector(context, this)

    var isReady = false
    var zoomMode: ZoomMode? = null
    var scaleFactor: Float = 1f
    var density: Float = 1f
    var listener: Listener? = null

    private var currentSpan: Float? = null
    private var internalInteractionMode = InteractionMode.Object

    private var lastPoint: PointF? = null
    private val isScrolling: Boolean
        get() = lastPoint != null
    private var canScroll = true
    private var canInteract = true
    private var isScaling = false

    private var isShiftPressed = false
    private var isCtrlPressed = false
    private var isAltPressed = false

    protected val keyModifier: Int
        get() {
            var modifier = 0
            if (isShiftPressed)
                modifier = modifier or CelestiaAppCore.SHIFT_KEY
            if (isCtrlPressed)
                modifier = modifier or CelestiaAppCore.CONTROL_KEY
            return modifier
        }

    private val button: Int
        get() = (if (isAltPressed) internalInteractionMode.next else internalInteractionMode).button

    fun setInteractionMode(interactionMode: InteractionMode) {
        CelestiaView.callOnRenderThread {
            internalInteractionMode = interactionMode
        }
    }

    fun callZoom() {
        val mode = zoomMode ?: return
        callZoom(mode.distance)
    }

    private fun callZoom(deltaY: Float) {
        if (internalInteractionMode == InteractionMode.Camera) {
            core.mouseMove(CelestiaAppCore.MOUSE_BUTTON_LEFT, PointF(0.0F, deltaY), CelestiaAppCore.SHIFT_KEY)
        } else {
            core.mouseWheel(deltaY, 0)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null || v == null) { return true }
        if (!isReady) { return true }

        if (!canInteract) {
            // Enable interaction back when last finger is lifted
            if (event.actionMasked == MotionEvent.ACTION_UP)
                canInteract = true
            return true
        }

        // Check first finger location before proceed
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            val point = PointF(event.x, event.y)

            val density = Resources.getSystem().displayMetrics.density

            var insetLeft = 16 * density
            var insetTop = 16 * density
            var insetRight = 16 * density
            var insetBottom = 16 * density

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                v.rootWindowInsets.displayCutout?.let {
                    insetLeft += it.safeInsetLeft
                    insetTop += it.safeInsetTop
                    insetRight += it.safeInsetRight
                    insetBottom += it.safeInsetBottom
                }
            }

            val interactionRect = RectF(insetLeft, insetTop, v.width - insetRight,  v.height - insetBottom)
            if (!interactionRect.contains(point.x, point.y)) {
                Log.d(TAG, "$interactionRect does not contain $point, interaction blocked")
                canInteract = false
                return true
            }
        }

        // First test if scaling is in progress
        scaleGestureDetector.onTouchEvent(event)
        if (isScaling) {
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                // Only mark scaling as ended when last finger is lifted
                isScaling = false
                canScroll = true
            }
            return true
        }

        if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            // Before detected as a scale, we might receiver ACTION_POINTER_DOWN, disable scrolling in advance
            canScroll = false
            return true
        }

        // Handle scroll and tap
        if (gestureDetector.onTouchEvent(event))
            return true

        if (event.actionMasked == MotionEvent.ACTION_UP && isScrolling) {
            // Last finger is lifted while scrolling
            Log.d(TAG, "on scroll end")

            stopScrolling()
            return true
        }

        // Other events
        Log.d(TAG, "unhandled event, ${event.actionMasked}")
        return true
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        isShiftPressed = false
        isCtrlPressed = false
        isAltPressed = false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        val det = detector ?: return true

        if (isScrolling)
            stopScrolling()

        Log.d(TAG, "on scale begin")

        currentSpan = det.currentSpan
        isScaling = true

        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        Log.d(TAG, "on scale end")
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        val det = detector ?: return true
        val previousSpan = currentSpan ?: return true
        val currentSpan = det.currentSpan

        Log.d(TAG, "on scale")

        val delta = det.currentSpan / previousSpan
        // FIXME: 8 is a magic number
        val deltaY = (1 - delta) * previousSpan / density / 8

        Log.d(TAG, "Pinch with deltaY: $deltaY")

        CelestiaView.callOnRenderThread {
            callZoom(deltaY)
        }

        this.currentSpan = currentSpan

        return true
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        val event = e ?: return true

        Log.d(TAG, "on single tap up")

        val point = PointF(event.x, event.y).scaleBy(scaleFactor)
        CelestiaView.callOnRenderThread {
            core.mouseButtonDown(CelestiaAppCore.MOUSE_BUTTON_LEFT, point, 0)
            core.mouseButtonUp(CelestiaAppCore.MOUSE_BUTTON_LEFT, point, 0)
        }

        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d(TAG, "on fling")
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!canScroll) return false

        val event1 = e1 ?: return true
        val event2 = e2 ?: return true

        Log.d(TAG, "on scroll")

        val offset = PointF(-distanceX, -distanceY).scaleBy(scaleFactor)
        val originalPoint = PointF(event1.x, event1.y).scaleBy(scaleFactor)
        val newPoint = PointF(event2.x, event2.y).scaleBy(scaleFactor)

        if (!isScrolling) {
            CelestiaView.callOnRenderThread {
                core.mouseButtonDown(button, originalPoint, keyModifier)
                core.mouseMove(button, offset, keyModifier)
            }
        } else {
            CelestiaView.callOnRenderThread {
                core.mouseMove(button, offset, keyModifier)
            }
        }
        lastPoint = newPoint
        return true
    }

    override fun onShowPress(e: MotionEvent?) {}

    override fun onLongPress(e: MotionEvent?) {
        if (e == null) return
        if (e.source == InputDevice.SOURCE_MOUSE) return // Mouse long press detected, ignore

        val viewLocation = PointF(e.x, e.y)
        listener?.showContextMenu(viewLocation.scaleBy(scaleFactor), viewLocation)
    }

    override fun onDown(e: MotionEvent?): Boolean {
        Log.d(TAG, "on down")
        return true
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (v == null || event == null) return false
        if (!isReady) return false

        if (event.action == KeyEvent.ACTION_UP)
            return onKeyUp(keyCode, event)
        else if (event.action == KeyEvent.ACTION_DOWN)
            return onKeyDown(keyCode, event)
        return false
    }

    private fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (!isReady) return false

        var input = event.unicodeChar
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT)
            isShiftPressed = true
        if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT)
            isCtrlPressed = true
        if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT)
            isAltPressed = true

        if (isCtrlPressed) {
            if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z)
                input = (keyCode - KeyEvent.KEYCODE_A) + 1
        }
        if (keyCode == KeyEvent.KEYCODE_ESCAPE)
            input = 27
        else if (keyCode == KeyEvent.KEYCODE_FORWARD_DEL)
            input = 127
        else if (keyCode == KeyEvent.KEYCODE_DEL)
            input = 8

        CelestiaView.callOnRenderThread {
            core.keyDown(input, keyCode, keyModifier)
        }
        return true
    }

    private fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (!isReady) return false
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT)
            isShiftPressed = false
        if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT)
            isCtrlPressed = false
        if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT)
            isAltPressed = false
        CelestiaView.callOnRenderThread {
            core.keyUp(event.unicodeChar, keyCode, keyModifier)
        }
        return true
    }

    private fun stopScrolling() {
        Log.d(TAG, "stop scrolling")
        val lp = lastPoint!!

        CelestiaView.callOnRenderThread {
            core.mouseButtonUp(button, lp, keyModifier)
        }
        lastPoint = null
    }

    companion object {
        private const val TAG = "CelestiaView"
    }
}

class CelestiaInteraction(context: Context): CelestiaBaseInteraction(context), View.OnGenericMotionListener, GestureDetector.OnContextClickListener {
    override fun onGenericMotion(v: View?, event: MotionEvent?): Boolean {
        if (event == null || v == null) { return true }
        if (!isReady) { return true }
        if (gestureDetector.onGenericMotionEvent(event)) return true

        if (event.action == MotionEvent.ACTION_SCROLL) {
            val y = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
            CelestiaView.callOnRenderThread {
                core.mouseWheel(y * scaleFactor, keyModifier)
            }
        }

        return true
    }

    override fun onContextClick(e: MotionEvent?): Boolean {
        if (e == null) return true

        val viewLocation = PointF(e.x, e.y)
        listener?.showContextMenu(viewLocation.scaleBy(scaleFactor), viewLocation)
        return true
    }
}