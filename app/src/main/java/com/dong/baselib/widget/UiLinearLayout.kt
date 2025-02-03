package com.dong.baselib.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.view.setPadding
import com.dong.baselib.R
import com.dong.baselib.base.GradientOrientation
import com.dong.baselib.canvas.drawRoundRectPath

@SuppressLint("CustomViewStyleable")
class UiLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var cornerRadius: Float = 0f
    private var stWidth: Float = 0f
    private var stColorDark: Int = Color.BLACK
    private var stColorLight: Int = Color.BLACK
    private var bgColorDark: Int = Color.TRANSPARENT
    private var bgColorLight: Int = Color.TRANSPARENT
    private var bgGradientStart: Int = Color.TRANSPARENT
    private var bgGradientEnd: Int = Color.TRANSPARENT
    private var bgGradientCenter: Int = Color.TRANSPARENT
    private var isGradient = false
    private var gradientOrientation: GradientDrawable.Orientation =
        GradientDrawable.Orientation.TOP_BOTTOM
    private var strokeGradientOrientation = GradientOrientation.LEFT_TO_RIGHT
    private var strokeGradient: IntArray? = null
    private val isDarkMode: Boolean
        get() {
            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        }
    private var clipContent: Boolean = true

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.StyleView)
            cornerRadius = typedArray.getDimension(R.styleable.StyleView_cornerRadius, 0f)
            stWidth = typedArray.getDimension(R.styleable.StyleView_strokeWidth, 0f)
            stColorDark = typedArray.getColor(R.styleable.StyleView_stColorDark, Color.BLACK)
            stColorLight = typedArray.getColor(R.styleable.StyleView_stColorLight, Color.BLACK)
            bgColorDark = typedArray.getColor(R.styleable.StyleView_bgColorDark, Color.TRANSPARENT)
            bgColorLight =
                typedArray.getColor(R.styleable.StyleView_bgColorLight, Color.TRANSPARENT)
            bgGradientStart =
                typedArray.getColor(R.styleable.StyleView_bgGradientStart, Color.TRANSPARENT)
            bgGradientCenter =
                typedArray.getColor(R.styleable.StyleView_bgGradientCenter, Color.TRANSPARENT)
            bgGradientEnd =
                typedArray.getColor(R.styleable.StyleView_bgGradientEnd, Color.TRANSPARENT)
            isGradient = bgGradientStart != Color.TRANSPARENT && bgGradientEnd != Color.TRANSPARENT


            val gradient = typedArray.getString(R.styleable.StyleView_strokeGradient)
            if (!gradient.isNullOrEmpty()) {
                strokeGradient = gradient.split(" ").map {
                    if (it.isValidHexColor()) {
                        Color.parseColor(it)
                    } else {
                        fromColor("00ffffff")
                    }
                }.toIntArray()
            }

            val orientation = context.obtainStyledAttributes(attrs, R.styleable.Orientation)
            gradientOrientation =
                when (orientation.getInt(R.styleable.Orientation_bgGdOrientation, 0)) {
                    1 -> GradientDrawable.Orientation.TR_BL
                    2 -> GradientDrawable.Orientation.RIGHT_LEFT
                    3 -> GradientDrawable.Orientation.BR_TL
                    4 -> GradientDrawable.Orientation.BOTTOM_TOP
                    5 -> GradientDrawable.Orientation.BL_TR
                    6 -> GradientDrawable.Orientation.LEFT_RIGHT
                    7 -> GradientDrawable.Orientation.TL_BR
                    else -> GradientDrawable.Orientation.TOP_BOTTOM
                }

            strokeGradientOrientation =
                when (orientation.getInt(R.styleable.Orientation_strokeGdOrientation, 6)) {
                    0 -> GradientOrientation.TOP_TO_BOTTOM
                    1 -> GradientOrientation.TR_BL
                    2 -> GradientOrientation.RIGHT_TO_LEFT
                    3 -> GradientOrientation.BR_TL
                    4 -> GradientOrientation.BOTTOM_TO_TOP
                    5 -> GradientOrientation.BL_TR
                    6 -> GradientOrientation.LEFT_TO_RIGHT
                    7 -> GradientOrientation.TL_BR
                    else -> GradientOrientation.TOP_TO_BOTTOM
                }
            orientation.recycle()
            clipContent = typedArray.getBoolean(
                R.styleable.StyleView_clipContent,
                true
            )
            setClipContent(clipContent)
            typedArray.recycle()
            if (isGradient) {
                updateGradient()
            } else {
                updateBackground()
            }
            setPadding(stWidth.toInt())
        }
        setWillNotDraw(false)
    }

    fun setGradientBg(start: Int, center: Int, end: Int) {
        bgGradientStart = start
        bgGradientCenter = center
        bgGradientEnd = end
        updateGradient()
    }

    private fun updateGradient() {
        val backgroundDrawable = GradientDrawable(
            gradientOrientation,
            if (bgGradientCenter != Color.TRANSPARENT) intArrayOf(
                bgGradientStart,
                bgGradientCenter,
                bgGradientEnd
            ) else intArrayOf(bgGradientStart, bgGradientEnd)
        ).apply {
            cornerRadius = this@UiLinearLayout.cornerRadius + 1.2f
        }
        background = backgroundDrawable
    }

    fun setGradientStroke(intArray: IntArray?) {
        this.strokeGradient = intArray
        postInvalidate()
        requestLayout()
    }

    fun setGradientStrokeOrientation(intArray: GradientOrientation) {
        strokeGradientOrientation = intArray
        postInvalidate()
        requestLayout()
    }

    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        (background as? GradientDrawable)?.cornerRadius = radius + 1.2f
    }

    fun setStrokeWidth(width: Int) {
        stWidth = width.toFloat()
        invalidate()
    }

    private fun updateBackground() {
        val backgroundColor = if (isDarkMode) bgColorDark else bgColorLight

        val backgroundDrawable = GradientDrawable().apply {
            setColor(backgroundColor)
            cornerRadius = this@UiLinearLayout.cornerRadius + 1.2f
        }
        background = backgroundDrawable
    }


    fun stColorDark(color: Int) {
        stColorDark = color
        postInvalidate()
    }

    fun stColor(@ColorInt light: Int, @ColorInt dark: Int) {
        stColorDark = dark
        stColorLight = light
        postInvalidate()
    }

    fun stColorLight(color: Int) {
        stColorLight = color
        postInvalidate()
    }

    fun setBgColorDark(color: Int) {
        bgGradientStart = color
        if (isDarkMode) {
            (background as? GradientDrawable)?.setColor(color)
        }
    }

    fun setBgColor(colorDark: Int, colorLight: Int) {
        bgGradientStart = colorDark
        bgGradientEnd = colorLight
        if (isDarkMode) {
            (background as? GradientDrawable)?.setColor(colorDark)
        } else {
            (background as? GradientDrawable)?.setColor(colorDark)
        }
    }

    fun setClipContent(clip: Boolean) {
        clipContent = clip
        clipToPadding = clip
        clipChildren = clip
        clipToOutline = clip

    }

    fun setBgColorLight(color: Int) {
        bgGradientEnd = color
        if (!isDarkMode) {
            (background as? GradientDrawable)?.setColor(color)
        }
    }


    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas) // Draws child views first
        if (stWidth > 0) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = this@UiLinearLayout.stWidth
            }
            if (strokeGradient != null) {
                if (strokeGradient!!.size > 1) {
                    val (x0, y0, x1, y1) = when (strokeGradientOrientation) {
                        GradientOrientation.TOP_TO_BOTTOM -> arrayOf(0f, 0f, 0f, height)
                        GradientOrientation.BOTTOM_TO_TOP -> arrayOf(0f, height, 0f, 0f)
                        GradientOrientation.LEFT_TO_RIGHT -> arrayOf(0f, 0f, width, 0f)
                        GradientOrientation.RIGHT_TO_LEFT -> arrayOf(width, 0f, 0f, 0f)
                        GradientOrientation.TL_BR -> arrayOf(0f, 0f, width, height)
                        GradientOrientation.TR_BL -> arrayOf(width, 0f, 0f, height)
                        GradientOrientation.BL_TR -> arrayOf(0f, height, width, 0f)
                        GradientOrientation.BR_TL -> arrayOf(width, height, 0f, 0f)
                    }

                    val gradient = LinearGradient(
                        x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(),
                        strokeGradient!!,
                        null,
                        Shader.TileMode.CLAMP
                    )
                    paint.shader = gradient
                } else {
                    paint.color = if (isDarkMode) stColorDark else stColorLight
                }
            } else {
                paint.color = if (isDarkMode) stColorDark else stColorLight
            }
            mBorderRectF = RectF(
                this@UiLinearLayout.stWidth / 2,
                this@UiLinearLayout.stWidth / 2,
                width.toFloat() - this@UiLinearLayout.stWidth / 2,
                height.toFloat() - this@UiLinearLayout.stWidth / 2
            )
            canvas.drawRoundRectPath(
                mBorderRectF,
                cornerRadius * 1.2f,
                true,
                true,
                true,
                true,
                paint
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                outlineProvider = OutlineProvider()
                clipToOutline = true
            }
        }
    }

    private var mBorderRectF = RectF(0f, 0f, 0f, 0f)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class OutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val mBorderRect = Rect(
                mBorderRectF.left.toInt(),
                mBorderRectF.top.toInt(),
                mBorderRectF.right.toInt(),
                mBorderRectF.bottom.toInt()
            )

            val minSize = minOf(mBorderRectF.width(),mBorderRectF.height())
            val conner = if(cornerRadius>minSize) minSize else cornerRadius
            outline.setRoundRect(mBorderRect, conner)
        }
    }

}