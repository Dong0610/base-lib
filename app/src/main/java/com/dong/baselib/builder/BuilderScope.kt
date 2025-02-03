package com.dong.baselib.builder

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData


data class Padding(
    var top: Float = 0f,
    var bottom: Float = 0f,
    var left: Float = 0f,
    var right: Float = 0f
) {

    fun value(value: Float = 0f): Padding {
        this.top = value
        this.bottom = value
        this.left = value
        this.right = value
        return this
    }

    fun value(horizontal: Float = 0f, vertical: Float = 0f): Padding {
        this.left = horizontal
        this.right = horizontal
        this.top = vertical
        this.bottom = vertical
        return this
    }

    fun value(top: Float = 0f, bottom: Float = 0f, left: Float = 0f, right: Float = 0f): Padding {
        this.top = top
        this.bottom = bottom
        this.left = left
        this.right = right
        return this
    }

    fun toPx(context: Context): IntArray {
        return intArrayOf(
            dpToPx(left, context),
            dpToPx(top, context),
            dpToPx(right, context),
            dpToPx(bottom, context)
        )
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}

data class Margin(
    var top: Float = 0f,
    var bottom: Float = 0f,
    var left: Float = 0f,
    var right: Float = 0f
) {

    fun value(value: Float = 0f): Margin {
        this.top = value
        this.bottom = value
        this.left = value
        this.right = value
        return this
    }

    fun value(horizontal: Float = 0f, vertical: Float = 0f): Margin {
        this.left = horizontal
        this.right = horizontal
        this.top = vertical
        this.bottom = vertical
        return this
    }

    fun value(top: Float = 0f, bottom: Float = 0f, left: Float = 0f, right: Float = 0f): Margin {
        this.top = top
        this.bottom = bottom
        this.left = left
        this.right = right
        return this
    }

    fun toPx(context: Context): IntArray {
        return intArrayOf(
            dpToPx(left, context),
            dpToPx(top, context),
            dpToPx(right, context),
            dpToPx(bottom, context)
        )
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}

class Gradient {
    var startColor: Int? = null
    var centerColor: Int? = null
    var endColor: Int? = null
    var gradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT

    fun createDrawable(): GradientDrawable {
        return GradientDrawable().apply {
            val colors = listOfNotNull(startColor, centerColor, endColor).toIntArray()
            orientation = gradientOrientation
            if (colors.isNotEmpty()) {
                setColors(colors)
            }
        }
    }
}

@ColorInt
fun fromColor(code: String): Int {
    val cleanedCode = code.replace("#", "").replace(" ", "")
    return android.graphics.Color.parseColor("#$cleanedCode")
}


class Background {
    var solidColor: Int = fromColor("ffffff")
    var gradient: Gradient? = null
}


class StrokeStyle {
    var solidColor: Int? = null
    var gradient: Gradient? = null
    var width: Float? = null

}

open class Style {

    var padding: MutableLiveData<Padding> = MutableLiveData(Padding())
    var margin: MutableLiveData<Margin> = MutableLiveData(Margin())
    var background: MutableLiveData<Background> = MutableLiveData(Background())
    var strokeStyle: MutableLiveData<StrokeStyle> = MutableLiveData(StrokeStyle())
    var width: MutableLiveData<Int> = MutableLiveData(ViewGroup.LayoutParams.MATCH_PARENT)
    var height: MutableLiveData<Int> = MutableLiveData(ViewGroup.LayoutParams.MATCH_PARENT)
    var gravity: MutableLiveData<Int> = MutableLiveData(Gravity.START)
    var contentAlignment: MutableLiveData<Int> = MutableLiveData(Gravity.TOP)
    var cornerRadius: MutableLiveData<Float> = MutableLiveData(0f)
    var clipContent = MutableLiveData(false)

    private var event: (() -> Unit)? = null

    fun padding(padding: Padding): Style {
        this.padding.value = (padding)
        return this
    }

    fun margin(margin: Margin): Style {
        this.margin.value = (margin)
        return this
    }

    fun background(background: Background): Style {
        this.background.value = (background)
        return this
    }
    fun background(background: Int): Style {
        val background= Background().apply {
            solidColor = background
            gradient=null
        }
        this.background.value = background
        return this
    }

    fun stroke(strokeStyle: StrokeStyle): Style {
        this.strokeStyle.value = (strokeStyle)
        return this
    }

    fun size(
        width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        height: Int = ViewGroup.LayoutParams.MATCH_PARENT
    ): Style {
        this.width.value = (width)
        this.height.value = (height)
        return this
    }

    fun align(gravity: Int): Style {
        this.gravity.value = (gravity)
        return this
    }

    fun contentAlign(contentAlignment: Int): Style {
        this.contentAlignment.value = (contentAlignment)
        return this
    }

    fun cornerRadius(radius: Float): Style {
        this.cornerRadius.value = (radius)
        return this
    }

    fun clipContent(clip: Boolean): Style {
        this@Style.clipContent.value = (clip)
        return this
    }

    fun onclick(event: () -> Unit): Style {
        this@Style.event = event
        return this
    }

    
    fun applyTo(view: View, context: Context) {
        
        padding.observe { applyPadding(view, context) }
        margin.observe { applyMargin(view, context) }
        background.observe { applyBackground(view) }
        strokeStyle.observe { applyStroke(view) }
        gravity.observe { t ->
            if (view is LinearLayout)
                t?.let {
                    view.gravity = it
                }
        }
        contentAlignment.observe {
            if (view is LinearLayout) it?.let { it1 ->
                view.setVerticalGravity(
                    it1
                )
            }
        }
        width.observe { newWidth ->
            newWidth?.let { widthValue ->
                val layoutParams = when (val viewParams = view.layoutParams) {
                    is LinearLayout.LayoutParams -> {
                        viewParams.width = widthValue
                        viewParams
                    }

                    is RelativeLayout.LayoutParams -> {
                        viewParams.width = widthValue
                        viewParams
                    }

                    is ConstraintLayout.LayoutParams -> {
                        viewParams.width = widthValue
                        viewParams
                    }

                    is FrameLayout.LayoutParams -> {
                        viewParams.width = widthValue
                        viewParams
                    }

                    is ViewGroup.LayoutParams -> {
                        viewParams.width = widthValue
                        viewParams
                    }

                    else -> null
                }
                if (layoutParams != null) {
                    view.layoutParams = layoutParams
                    Log.w(
                        "LayoutParams",
                        "Data Size: ${layoutParams.width}x${layoutParams.height}"
                    )
                    view.requestLayout()
                } else {
                    Log.w(
                        "LayoutParams",
                        "Unsupported layout type for view: ${view.javaClass.simpleName}"
                    )
                }
            }
        }


        this@Style.clipContent.observe {
            if (view is ViewGroup) {
                if (it != null) {
                    view.clipChildren = it
                }
            }
        }

        height.observe { newHeight ->
            val layoutParams = when (val viewParams = view.layoutParams) {
                is LinearLayout.LayoutParams -> {
                    viewParams.height = newHeight ?: viewParams.height
                    viewParams
                }

                is RelativeLayout.LayoutParams -> {
                    viewParams.height = newHeight ?: viewParams.height
                    viewParams
                }

                is ConstraintLayout.LayoutParams -> {
                    viewParams.height = newHeight ?: viewParams.height
                    viewParams
                }

                is FrameLayout.LayoutParams -> {
                    viewParams.height = newHeight ?: viewParams.height
                    viewParams
                }

                is ViewGroup.LayoutParams -> {
                    viewParams.height = newHeight ?: viewParams.height
                    viewParams
                }

                else -> null
            }
            if (layoutParams != null) {
                view.layoutParams = layoutParams
                Log.w(
                    "LayoutParams",
                    "Data Size: ${layoutParams.width}x${layoutParams.height}"
                )
                view.requestLayout()
            } else {
                Log.w(
                    "LayoutParams",
                    "Unsupported layout type for view: ${view.javaClass.simpleName}"
                )
            }
        }


        cornerRadius.observe { t ->
            t?.let {
                applyCornerRadius(view, it)
            }
        }

        view.setOnClickListener {
            event?.invoke()
        }
    }

    
    fun applyPadding(view: View, context: Context) {
        padding.value?.let {
            val paddingPx = it.toPx(context)
            view.setPadding(paddingPx[0], paddingPx[1], paddingPx[2], paddingPx[3])
        }
    }

    
    fun applyMargin(view: View, context: Context) {
        margin.value?.let {
            if (view.layoutParams is ViewGroup.MarginLayoutParams) {
                val marginPx = it.toPx(context)
                (view.layoutParams as ViewGroup.MarginLayoutParams).setMargins(
                    marginPx[0], marginPx[1], marginPx[2], marginPx[3]
                )
            }
        }
    }

    
    fun applyBackground(view: View) {
        background.value?.let {
            view.background = it.gradient?.createDrawable() ?: GradientDrawable().apply {
                setColor(it.solidColor)
                cornerRadius = this@Style.cornerRadius.value ?: 0f
            }
        }
    }

    
    fun applyStroke(view: View) {
        strokeStyle.value?.let {
            if (it.width != null && it.solidColor != null) {
                val drawable = (view.background as? GradientDrawable) ?: GradientDrawable()
                drawable.setStroke(it.width!!.toInt(), it.solidColor!!)
                drawable.cornerRadius = this.cornerRadius.value ?: 0f
                view.background = drawable
            }
        }
    }

    
    fun applyCornerRadius(view: View, radius: Float) {
        (view.background as? GradientDrawable)?.cornerRadius = radius
    }

    
    private fun <T> MutableLiveData<T>.observe(action: (T?) -> Unit) {
        this.observeForever { value -> action(value) }
    }
}

class BuilderScope(private val context: Context, private val parent: View) {

    fun Column(style: Style = Style(), init: BuilderScope.() -> Unit) {
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        style.applyTo(column, context)
        BuilderScope(context, column)

        if (parent is ViewGroup) {
            parent.addView(column)
        }

    }

    fun Row(style: Style = Style(), init: BuilderScope.() -> Unit) {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        style.applyTo(row, context)
        BuilderScope(context, row).init()
        if (parent is ViewGroup) {
            parent.addView(row)
        }
    }

    fun Box(style: Style = Style(), init: BuilderScope.() -> Unit) {
        val box = RelativeLayout(context)
        style.applyTo(box, context)
        BuilderScope(context, box).init()
        if (parent is ViewGroup) {
            parent.addView(box)
        }
    }

    fun Text(text: String, style: TextStyle = TextStyle(), onClick: (() -> Unit)? = null) {
        val textView = TextView(context).apply {
            setText(text)
        }
        style.applyToText(textView, context)
        textView.setOnClickListener {
            onClick?.invoke()
        }
        if (parent is ViewGroup) {
            parent.addView(textView)
        }
    }

    fun Button(text: String, style: Style = Style(), onClick: (() -> Unit)? = null) {
        val button = android.widget.Button(context).apply {
            setText(text)
            setOnClickListener { onClick?.invoke() }
        }
        style.applyTo(button, context)
        if (parent is ViewGroup) {
            parent.addView(button)
        }
    }

    fun Image(resourceId: Int, style: Style = Style(), onClick: (() -> Unit)? = null) {
        val imageView = ImageView(context).apply {
            setImageResource(resourceId)
            setOnClickListener {
                onClick?.invoke()
            }
        }
        style.applyTo(imageView, context)
        if (parent is ViewGroup) {
            parent.addView(imageView)
        }
    }

    fun Spacer(width: Int = 0, height: Int = 0, onClick: (() -> Unit)? = null) {
        val spacer = View(context).apply {
            layoutParams = ViewGroup.LayoutParams(width, height)
            setOnClickListener {
                onClick?.invoke()
            }
        }
        if (parent is ViewGroup) {
            parent.addView(spacer)
        }
    }
}

fun View.content(style: Style = Style(), builder: BuilderScope.() -> Unit) {
    style.applyTo(this, context)
    BuilderScope(context, this).apply(builder)
}