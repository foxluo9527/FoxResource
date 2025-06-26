package com.foxluo.baselib.util

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.PopupWindow
import com.foxluo.baselib.R
import androidx.core.graphics.drawable.toDrawable

interface KeyboardHeightObserver {
    fun onKeyboardHeightChanged(height: Int, orientation: Int)

    fun onVirtualBottomHeight(height: Int)
}

class KeyboardHeightProvider(activity: Activity) : PopupWindow(activity) {
    private var observer: KeyboardHeightObserver? = null

    private var keyboardLandscapeHeight = 0

    private var keyboardPortraitHeight = 0

    private var popupView: View?

    private lateinit var parentView: View

    private val activity: Activity

    init {
        this.activity = activity

        val inflater: LayoutInflater =
            activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.popupView = inflater.inflate(R.layout.keyboard_popup_window, null, false)
        setContentView(popupView)

        setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE or LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        setInputMethodMode(INPUT_METHOD_NEEDED)



        setWidth(0)
        setHeight(LayoutParams.MATCH_PARENT)

        popupView?.getViewTreeObserver()?.addOnGlobalLayoutListener({
            if (popupView != null) {
                handleOnGlobalLayout()
            }
        })
    }

    fun start(contentViewId: Int) {
        parentView = activity.findViewById(contentViewId)
        if (!isShowing() && parentView.getWindowToken() != null) {
            setBackgroundDrawable(0.toDrawable())
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0)
        }
    }

    fun close() {
        this.observer = null
        dismiss()
    }

    fun setKeyboardHeightObserver(observer: KeyboardHeightObserver?) {
        this.observer = observer
    }

    private val screenOrientation: Int
        get() = activity.getResources().getConfiguration().orientation

    private fun handleOnGlobalLayout() {
        val screenSize: Point = Point()
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize)

        val rect: Rect = Rect()
        popupView?.getWindowVisibleDisplayFrame(rect)

        val orientation = this.screenOrientation
        val keyboardHeight: Int = screenSize.y - rect.bottom

        if (keyboardHeight < 0 && observer != null) {
            observer?.onVirtualBottomHeight(-keyboardHeight)
        }

        if (keyboardHeight == 0) {
            notifyKeyboardHeightChanged(0, orientation)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.keyboardPortraitHeight = keyboardHeight
            notifyKeyboardHeightChanged(keyboardPortraitHeight, orientation)
        } else {
            this.keyboardLandscapeHeight = keyboardHeight
            notifyKeyboardHeightChanged(keyboardLandscapeHeight, orientation)
        }
    }

    private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
        if (observer != null) {
            observer?.onKeyboardHeightChanged(height, orientation)
        }
    }
}