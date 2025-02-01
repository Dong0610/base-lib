package com.dong.baselibrary.base

import android.content.Context
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.viewbinding.ViewBinding


inline fun <reified VB : ViewBinding> showPopup(
    anchorView: View,
    crossinline bindingInflater: (LayoutInflater) -> VB,
    width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    height: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    locationX: Int,
    locationY: Int,
    crossinline onViewBinder: (VB, PopupWindow) -> Unit
) {
    val inflater = LayoutInflater.from(anchorView.context)
    val popupView = bindingInflater(inflater)
    val rootView = popupView.root
    val popupWindow = PopupWindow(rootView, width, height, true)

    onViewBinder(popupView, popupWindow)
    popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, locationX, locationY)
}


class Popup<VB : ViewBinding> private constructor(
    private val context: Context,
    private val bindingInflater: (LayoutInflater) -> VB
) {

    private var width: Int = WindowManager.LayoutParams.WRAP_CONTENT
    private var height: Int = WindowManager.LayoutParams.WRAP_CONTENT
    private var locationX: Int = 0
    private var locationY: Int = 0
    private var onViewBinder: ((VB, PopupWindow) -> Unit)? = null

    companion object {
        fun <VB : ViewBinding> inflater(
            bindingInflater: (LayoutInflater) -> VB,
            context: Context
        ): Popup<VB> {
            return Popup(context, bindingInflater)
        }
    }

    fun size(sizeProvider: (width: Int, height: Int) -> Unit): Popup<VB> {
        val inflater = LayoutInflater.from(context)
        val popupView = bindingInflater(inflater).root
        popupView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        sizeProvider(popupView.measuredWidth, popupView.measuredHeight)
        return this
    }

    private var autoClose: Boolean = false
    private var time: Long = 1500

    fun autoClose(autoClose: Boolean = false, time: Long = 1500): Popup<VB> {
        this.autoClose = autoClose
        this.time = time
        return this
    }

    fun resetSize(
        newWidth: Int = WindowManager.LayoutParams.WRAP_CONTENT,
        newHeight: Int = WindowManager.LayoutParams.WRAP_CONTENT
    ): Popup<VB> {
        width = newWidth
        height = newHeight
        return this
    }


    fun location(x: Int, y: Int): Popup<VB> {
        locationX = x
        locationY = y
        return this
    }

    fun setView(onViewBinder: (VB, PopupWindow) -> Unit): Popup<VB> {
        this.onViewBinder = onViewBinder
        return this
    }

    fun show() {
        val inflater = LayoutInflater.from(context)
        val popupView = bindingInflater(inflater)
        val rootView = popupView.root
        val popupWindow = PopupWindow(rootView, width, height, true)

        onViewBinder?.invoke(popupView, popupWindow)
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, locationX, locationY)

        if (autoClose) {
            Handler().postDelayed({
                popupWindow.dismiss()
            }, time)
        }

    }
}
