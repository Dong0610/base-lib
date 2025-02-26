package com.dong.baselib.widget.layout

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.dong.baselib.R
import com.dong.baselib.widget.GradientOrientation
import com.dong.baselib.widget.fromColor
import com.dong.baselib.widget.isValidHexColor
import kotlin.math.min

class UiFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr)
{

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
    private var isDistance = false
    private var distanceSpace = 10f

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
            isDistance = typedArray.getBoolean(R.styleable.StyleView_strokeDistance, false)
            distanceSpace = typedArray.getDimension(R.styleable.StyleView_distanceSpace, 10f)

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

            gradientOrientation =
                when (typedArray.getInt(R.styleable.StyleView_bgGdOrientation, 0)) {
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
                when (typedArray.getInt(R.styleable.StyleView_strokeGdOrientation, 6)) {
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

            typedArray.recycle()
            if (isGradient) {
                updateGradient()
            } else {
                updateBackground()
            }

            // Ensure proper padding
            setPadding(
                paddingLeft + stWidth.toInt(),
                paddingTop + stWidth.toInt(),
                paddingRight + stWidth.toInt(),
                paddingBottom + stWidth.toInt()
            )
        }
        setWillNotDraw(false)
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
            cornerRadius = this@UiFrameLayout.cornerRadius
        }
        background = backgroundDrawable
    }



    private fun updateBackground() {
        val backgroundColor = if (isDarkMode) bgColorDark else bgColorLight

        val backgroundDrawable = GradientDrawable().apply {
            setColor(backgroundColor)
            cornerRadius = this@UiFrameLayout.cornerRadius
        }
        background = backgroundDrawable
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas) // Draws child views first

        if (stWidth > 0) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = this@UiFrameLayout.stWidth
                isAntiAlias = true
                isDither = true
                strokeJoin = Paint.Join.ROUND
                if (isDistance) {
                    pathEffect = DashPathEffect(floatArrayOf(distanceSpace, distanceSpace), 0f)
                }
            }

            if (strokeGradient != null && strokeGradient!!.size > 1) {
                val (x0, y0, x1, y1) = when (strokeGradientOrientation) {
                    GradientOrientation.TOP_TO_BOTTOM -> arrayOf(0f, 0f, 0f, height.toFloat())
                    GradientOrientation.BOTTOM_TO_TOP -> arrayOf(0f, height.toFloat(), 0f, 0f)
                    GradientOrientation.LEFT_TO_RIGHT -> arrayOf(0f, 0f, width.toFloat(), 0f)
                    GradientOrientation.RIGHT_TO_LEFT -> arrayOf(width.toFloat(), 0f, 0f, 0f)
                    GradientOrientation.TL_BR -> arrayOf(0f, 0f, width.toFloat(), height.toFloat())
                    GradientOrientation.TR_BL -> arrayOf(width.toFloat(), 0f, 0f, height.toFloat())
                    GradientOrientation.BL_TR -> arrayOf(0f, height.toFloat(), width.toFloat(), 0f)
                    GradientOrientation.BR_TL -> arrayOf(width.toFloat(), height.toFloat(), 0f, 0f)
                }

                val gradient = LinearGradient(
                    x0, y0, x1, y1,
                    strokeGradient!!,
                    null,
                    Shader.TileMode.CLAMP
                )
                paint.shader = gradient
            } else {
                paint.color = if (isDarkMode) stColorDark else stColorLight
            }

            val inset = stWidth / 2
            val borderRectF = RectF(
                inset, inset, width.toFloat() - inset, height.toFloat() - inset
            )

            val minSize = minOf(borderRectF.width() / 2, borderRectF.height() / 2)
            val corner = min(cornerRadius, minSize) // Prevent over-rounding

            canvas.drawRoundRect(borderRectF, corner, corner, paint)
        }
    }

    fun setGradientBg(start: Int, center: Int, end: Int) {
        bgGradientStart = start
        bgGradientCenter = center
        bgGradientEnd = end
        updateGradient()
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

    fun stColorDark(color: Int) {
        stColorDark = color
        postInvalidate()
    }

    fun stColor(@ColorInt light: Int, @ColorInt dark: Int) {
        stColorDark = dark
        stColorLight = light
        postInvalidate()
    }

    fun stColor(@ColorInt color: Int) {
        stColorDark = color
        stColorLight = color
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
}
