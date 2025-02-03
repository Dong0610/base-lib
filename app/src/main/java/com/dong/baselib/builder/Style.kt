package  com.dong.baselib.builder

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.text.InputFilter
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.dong.baselib.lifecycle.change
import com.dong.baselib.lifecycle.mutableLiveData


class StyleBuilder(private val context: Context) {
    var background: Background? = null
    var strokeStyle: StrokeStyle? = null
    var padding: Padding? = Padding()
    var margin: Margin? = Margin()
    var radius: MutableLiveData<Float> = mutableLiveData(0f)
    var src: Int? = null 
    var scaleType: ImageView.ScaleType? = null
    var adjustViewBounds: Boolean? = null
    var tint: Int? = null 
    var maxWidth: Int? = null 
    var maxHeight: Int? = null 
    var text: String? = null
    var hint: String? = null
    var textColor: Int? = null
    var textColorHighlight: Int? = null
    var textColorHint: Int? = null
    var textSize: Int? = null 
    var textScaleX: Float? = null
    var textStyle: Int = Typeface.NORMAL
    var fontFamily: String? = null
    var cursorVisible: Boolean = true
    var maxLines: Int? = null
    var minLines: Int? = null
    var minHeight: Int? = null
    var maxEms: Int? = null
    var minEms: Int? = null
    var gravity: Int? = null
    var password: Boolean = false
    var singleLine: Boolean = false
    var includeFontPadding: Boolean = true
    var maxLength: Int? = null
    var editable: Boolean = false
    var inputType: Int? = null
    var textAllCaps: Boolean = false
    var letterSpacing: Float? = null

    fun apply(view: View) {

        val backgroundDrawable = GradientDrawable().apply {
            background?.let {
                it.solidColor.let { color -> setColor(color) }
                it.gradient?.let { gradient ->
                    setColor(0)
                    gradient.createDrawable().let { gradientDrawable ->
                        setColors(gradientDrawable.colors)
                        orientation = gradientDrawable.orientation
                    }
                }
                this@StyleBuilder.radius.change {
                    setCornerRadius(dpToPx(it*1.009f).toFloat())
                }
            }
        }

        val strokeDrawable = if (strokeStyle?.gradient != null) {
            GradientDrawable().apply {
                strokeStyle?.gradient?.let { gradient ->
                    setColors(listOfNotNull(gradient.startColor, gradient.centerColor, gradient.endColor).toIntArray())
                    orientation = gradient.gradientOrientation
                }
                setStroke(dpToPx(strokeStyle?.width ?: 0f), Color.TRANSPARENT)
                this@StyleBuilder.radius.change {
                    setCornerRadius(dpToPx(it).toFloat())
                }
            }
        } else {
            GradientDrawable().apply {
                strokeStyle?.solidColor?.let { color ->
                    setStroke(dpToPx(strokeStyle?.width ?: 0f), color)
                }
                this@StyleBuilder.radius.change {
                    setCornerRadius(dpToPx(it).toFloat())
                }
            }
        }
        val layerDrawable = LayerDrawable(arrayOf(strokeDrawable, backgroundDrawable)).apply {
            strokeStyle?.width?.let { strokeWidth ->
                val strokeWidthPx = dpToPx(strokeWidth)
                setLayerInset(1, strokeWidthPx, strokeWidthPx, strokeWidthPx, strokeWidthPx)
            }
        }
        view.background = layerDrawable

        padding?.let {
            val (left, top, right, bottom) = it.toPx(context)
            view.setPadding(left, top, right, bottom)
        }
        if (view.layoutParams is ViewGroup.MarginLayoutParams && margin != null) {
            val (left, top, right, bottom) = margin!!.toPx(context)
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(left, top, right, bottom)
            view.layoutParams = layoutParams
        }

        if (view is ImageView) {
            src?.let { view.setImageResource(it) }
            scaleType?.let { view.scaleType = it }
            adjustViewBounds?.let { view.adjustViewBounds = it }
            tint?.let {
                try {
                    view.setColorFilter(ContextCompat.getColor(context, it))
                } catch (e: Resources.NotFoundException) {

                    view.setColorFilter(it)
                }
            }
            maxWidth?.let { view.maxWidth = dpToPx(it) }
            maxHeight?.let { view.maxHeight = dpToPx(it) }
        }

        if (view is TextView) {
            text?.let { view.text = it }
            hint?.let { view.hint = it }
            textColor?.let { view.setTextColor(it) }
            textColorHighlight?.let { view.setHighlightColor(it) }
            textColorHint?.let { view.setHintTextColor(it) }
            textSize?.let { view.setTextSize(TypedValue.COMPLEX_UNIT_SP, it.toFloat()) }
            textScaleX?.let { view.scaleX = it }
            view.typeface = Typeface.create(fontFamily, textStyle)
            view.isCursorVisible = cursorVisible
            maxLines?.let { view.maxLines = it }
            minLines?.let { view.minLines = it }
            minHeight?.let { view.minimumHeight = dpToPx(it) }
            maxHeight?.let { view.maxHeight = dpToPx(it) }
            minEms?.let { view.minEms = it }
            maxEms?.let { view.maxEms = it }
            gravity?.let { view.gravity = it }
            view.isSingleLine = singleLine
            view.includeFontPadding = includeFontPadding
            maxLength?.let { view.filters = arrayOf(InputFilter.LengthFilter(it)) }
            if (editable) {
                view.isFocusableInTouchMode = true
                view.isFocusable = true
            }
            if (inputType != null) view.inputType = inputType!!
            view.isAllCaps = textAllCaps
            letterSpacing?.let { view.letterSpacing = it }
        }
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private fun dpToPx(dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
fun View.setStyle(
    styleBuilder: StyleBuilder.() -> Unit,
) {
    val builder = StyleBuilder(context)
    builder.styleBuilder()
    builder.apply(this)
}


