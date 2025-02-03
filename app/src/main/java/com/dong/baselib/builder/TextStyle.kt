package com.dong.baselib.builder

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData

class TextStyle {
    var hint: MutableLiveData<String?> = MutableLiveData(null)
    var textColor: MutableLiveData<Int?> = MutableLiveData(fromColor("000000"))
    var textColorHighlight: MutableLiveData<Int?> = MutableLiveData(null)
    var textColorHint: MutableLiveData<Int?> = MutableLiveData(null)
    var textSize: MutableLiveData<Int?> = MutableLiveData(16)
    var textScaleX: MutableLiveData<Float?> = MutableLiveData(null)
    var textStyle: MutableLiveData<Int> = MutableLiveData(Typeface.NORMAL)
    var fontFamily: MutableLiveData<String?> = MutableLiveData(null)
    var cursorVisible: MutableLiveData<Boolean> = MutableLiveData(true)
    var maxLines: MutableLiveData<Int?> = MutableLiveData(null)
    var minLines: MutableLiveData<Int?> = MutableLiveData(null)
    var minHeight: MutableLiveData<Int?> = MutableLiveData(null)
    var maxEms: MutableLiveData<Int?> = MutableLiveData(null)
    var minEms: MutableLiveData<Int?> = MutableLiveData(null)
    var password: MutableLiveData<Boolean> = MutableLiveData(false)
    var singleLine: MutableLiveData<Boolean> = MutableLiveData(false)
    var includeFontPadding: MutableLiveData<Boolean> = MutableLiveData(true)
    var maxLength: MutableLiveData<Int?> = MutableLiveData(null)
    var editable: MutableLiveData<Boolean> = MutableLiveData(true)
    var inputType: MutableLiveData<Int?> = MutableLiveData(null)
    var textAllCaps: MutableLiveData<Boolean> = MutableLiveData(false)
    var letterSpacing: MutableLiveData<Float?> = MutableLiveData(null)

    var padding: MutableLiveData<Padding> = MutableLiveData(Padding())
    var margin: MutableLiveData<Margin> = MutableLiveData(Margin())
    var background: MutableLiveData<Background> = MutableLiveData(Background())
    var strokeStyle: MutableLiveData<StrokeStyle> = MutableLiveData(StrokeStyle())
    var width: MutableLiveData<Int> = MutableLiveData(ViewGroup.LayoutParams.WRAP_CONTENT)
    var height: MutableLiveData<Int> = MutableLiveData(ViewGroup.LayoutParams.WRAP_CONTENT)
    var gravity: MutableLiveData<Int> = MutableLiveData(Gravity.START)
    var contentAlignment: MutableLiveData<Int> = MutableLiveData(Gravity.TOP)
    var cornerRadius: MutableLiveData<Float> = MutableLiveData(0f) 

    
    fun hint(value: String) = apply { hint.postValue(value) }
    fun textColor(value: Int) = apply { textColor.postValue(value) }
    fun textColorHighlight(value: Int) = apply { textColorHighlight.postValue(value) }
    fun textColorHint(value: Int) = apply { textColorHint.postValue(value) }
    fun textSize(value: Int) = apply { textSize.postValue(value) }
    fun textScaleX(value: Float) = apply { textScaleX.postValue(value) }
    fun textStyle(value: Int) = apply { textStyle.postValue(value) }
    fun fontFamily(value: String) = apply { fontFamily.postValue(value) }
    fun cursorVisible(value: Boolean) = apply { cursorVisible.postValue(value) }
    fun maxLines(value: Int) = apply { maxLines.postValue(value) }
    fun minLines(value: Int) = apply { minLines.postValue(value) }
    fun minHeight(value: Int) = apply { minHeight.postValue(value) }
    fun maxEms(value: Int) = apply { maxEms.postValue(value) }
    fun minEms(value: Int) = apply { minEms.postValue(value) }
    fun password(value: Boolean) = apply { password.postValue(value) }
    fun singleLine(value: Boolean) = apply { singleLine.postValue(value) }
    fun includeFontPadding(value: Boolean) = apply { includeFontPadding.postValue(value) }
    fun maxLength(value: Int) = apply { maxLength.postValue(value) }
    fun editable(value: Boolean) = apply { editable.postValue(value) }
    fun inputType(value: Int) = apply { inputType.postValue(value) }
    fun textAllCaps(value: Boolean) = apply { textAllCaps.postValue(value) }
    fun letterSpacing(value: Float) = apply { letterSpacing.postValue(value) }
    fun width(value: Int) = apply { width.postValue(value) }
    fun height(value: Int) = apply { height.postValue(value) }
    fun gravity(value: Int) = apply { gravity.postValue(value) }
    fun contentAlignment(value: Int) = apply { contentAlignment.postValue(value) }
    fun cornerRadius(value: Float) = apply { cornerRadius.postValue(value) }
    fun padding(value: Padding) = apply { padding.postValue(value) }
    fun background(background: Background)= apply { this@TextStyle.background.postValue(background)}
    fun stroke(stroke: StrokeStyle) = apply{ this@TextStyle.strokeStyle.postValue(stroke)}
    fun margin(value: Margin) = apply { this@TextStyle.margin.postValue(value) }

    fun applyToText(view: TextView, context: Context) {
        hint.observe { view.hint = it }
        textColor.observe { view.setTextColor(it ?: Color.BLACK) }
        textColorHighlight.observe { view.highlightColor = it ?: Color.TRANSPARENT }
        textColorHint.observe { view.setHintTextColor(it ?: Color.GRAY) }
        textSize.observe { it?.let { size -> view.textSize = size.toFloat() } }
        textScaleX.observe { it?.let { scale -> view.textScaleX = scale } }
        textStyle.observe { view.setTypeface(view.typeface, it ?: Typeface.NORMAL) }
        fontFamily.observe {
            it?.let { family ->
                view.typeface = Typeface.create(family, view.typeface.style)
            }
        }
        cursorVisible.observe {
            if (it != null) {
                view.isCursorVisible = it
            }
        }
        maxLines.observe { view.maxLines = it ?: Int.MAX_VALUE }
        minLines.observe { view.minLines = it ?: 1 }
        minHeight.observe { view.minHeight = it ?: 0 }
        password.observe {
            view.transformationMethod =
                if (it == true) PasswordTransformationMethod.getInstance() else null
        }
        singleLine.observe {
            if (it != null) {
                view.isSingleLine = it
            }
        }
        includeFontPadding.observe {
            if (it != null) {
                view.includeFontPadding = it
            }
        }
        maxLength.observe {
            it?.let { length ->
                view.filters = arrayOf(InputFilter.LengthFilter(length))
            }
        }
        editable.observe {
            if (it != null) {
                view.isEnabled = it
            }
        }
        inputType.observe { it?.let { type -> view.inputType = type } }
        textAllCaps.observe {
            if (it != null) {
                view.isAllCaps = it
            }
        }
        letterSpacing.observe { it?.let { spacing -> view.letterSpacing = spacing } }
        gravity.observe { view.gravity = it ?: Gravity.START }
        contentAlignment.observe {  }
        cornerRadius.observe {  }
        padding.observe { applyPadding(view, context) }
        margin.observe { applyMargin(view, context) }
        background.observe { applyBackground(view) }
        strokeStyle.observe { applyStroke(view) }
        view.requestLayout()
    }

    private fun applyPadding(view: View, context: Context) {
        val paddingPx = padding.value?.toPx(context)
        paddingPx?.let {
            view.setPadding(paddingPx[0], paddingPx[1], paddingPx[2], paddingPx[3])
        }
    }

    private fun applyMargin(view: View, context: Context) {
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            val marginPx = margin.value?.toPx(context)
            marginPx?.let {
                (view.layoutParams as ViewGroup.MarginLayoutParams).setMargins(
                    marginPx[0], marginPx[1], marginPx[2], marginPx[3]
                )
            }

        }
    }

    private fun applyBackground(view: View) {
        view.background = this@TextStyle.background.value?.gradient?.createDrawable()
            ?: GradientDrawable().apply {
                this@TextStyle.background.value?.solidColor?.let { setColor(it) }
                this@TextStyle.cornerRadius.value?.let {
                    cornerRadius = it
                }
            }
    }

    private fun applyStroke(view: View) {
        if (strokeStyle.value?.width != null && strokeStyle.value?.solidColor != null) {
            val drawable = (view.background as? GradientDrawable) ?: GradientDrawable()
            drawable.setStroke(strokeStyle.value?.width!!.toInt(), strokeStyle.value?.solidColor!!)
            this@TextStyle.cornerRadius.value?.let {
                drawable.cornerRadius = it
            }
            view.background = drawable
        }
    }

    private fun <T> MutableLiveData<T>.observe(action: (T?) -> Unit) {
        this.observeForever { value -> action(value) }
    }

}
