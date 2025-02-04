package  com.dong.baselib.widget

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
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import com.dong.baselib.R
import com.dong.baselib.canvas.drawRoundRectPath

enum class TextGradientOrientation {
    TOP_TO_BOTTOM,
    TR_BL,
    RIGHT_TO_LEFT,
    BR_TL,
    BOTTOM_TO_TOP,
    BL_TR,
    LEFT_TO_RIGHT,
    TL_BR
}


fun TextView.setGradient(
    startColor: Int,
    endColor: Int,
    orientation: TextGradientOrientation = TextGradientOrientation.LEFT_TO_RIGHT
) {
    val width = paint.measureText(text.toString())
    val height = textSize * 1.3f

    val (x0, y0, x1, y1) = when (orientation) {
        TextGradientOrientation.TOP_TO_BOTTOM -> arrayOf(0f, 0f, 0f, height)
        TextGradientOrientation.BOTTOM_TO_TOP -> arrayOf(0f, height, 0f, 0f)
        TextGradientOrientation.LEFT_TO_RIGHT -> arrayOf(0f, 0f, width, 0f)
        TextGradientOrientation.RIGHT_TO_LEFT -> arrayOf(width, 0f, 0f, 0f)
        TextGradientOrientation.TL_BR -> arrayOf(0f, 0f, width, height)
        TextGradientOrientation.TR_BL -> arrayOf(width, 0f, 0f, height)
        TextGradientOrientation.BL_TR -> arrayOf(0f, height, width, 0f)
        TextGradientOrientation.BR_TL -> arrayOf(width, height, 0f, 0f)
    }

    val gradient = LinearGradient(
        x0, y0, x1, y1,
        intArrayOf(startColor, endColor),
        null,
        Shader.TileMode.CLAMP
    )

    paint.shader = gradient
    invalidate()
}


class UiTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var tColorDark: Int = currentTextColor
    private var tColorLight: Int = currentTextColor
    private var tColorHint: Int = currentHintTextColor
    private var underlineText: Boolean = false

    private var cornerRadius: Float = 0f
    private var stWidth: Float = 0f
    private var stColorDark: Int = Color.TRANSPARENT
    private var stColorLight: Int = Color.TRANSPARENT
    private var bgColorDark: Int = Color.TRANSPARENT
    private var bgColorLight: Int = Color.TRANSPARENT
    private var bgGradientStart: Int = Color.TRANSPARENT
    private var bgGradientCenter: Int = Color.TRANSPARENT
    private var bgGradientEnd: Int = Color.TRANSPARENT
    private var textGradientStart: Int = Color.TRANSPARENT
    private var textGradientEnd: Int = Color.TRANSPARENT
    private var textGradientOrientation: Int = 0
    private var textGradient: Boolean = false
    private var isGradient = false
    private var gradientOrientation: GradientDrawable.Orientation =
        GradientDrawable.Orientation.TOP_BOTTOM

    private fun isDarkMode(): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    private var strokeGradientOrientation = GradientOrientation.LEFT_TO_RIGHT
    private var strokeGradient: IntArray? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StyleView,
            defStyleAttr,
            0
        ).apply {
            val textViewAttrs = context.obtainStyledAttributes(attrs, R.styleable.UiTextView)
            cornerRadius = textViewAttrs.getDimension(R.styleable.UiTextView_cornerRadius, 0f)
            stWidth = textViewAttrs.getDimension(R.styleable.UiTextView_strokeWidth, 0f)
            stColorDark = textViewAttrs.getColor(R.styleable.UiTextView_stColorDark, Color.BLACK)
            stColorLight = textViewAttrs.getColor(R.styleable.UiTextView_stColorLight, Color.BLACK)
            bgColorDark = textViewAttrs.getColor(R.styleable.UiTextView_bgColorDark, Color.TRANSPARENT)
            bgColorLight =
                textViewAttrs.getColor(R.styleable.UiTextView_bgColorLight, Color.TRANSPARENT)
            bgGradientStart =
                textViewAttrs.getColor(R.styleable.UiTextView_bgGradientStart, Color.TRANSPARENT)
            bgGradientCenter =
                textViewAttrs.getColor(R.styleable.UiTextView_bgGradientCenter, Color.TRANSPARENT)
            bgGradientEnd =
                textViewAttrs.getColor(R.styleable.UiTextView_bgGradientEnd, Color.TRANSPARENT)
            isGradient = bgGradientStart != Color.TRANSPARENT && bgGradientEnd != Color.TRANSPARENT

            gradientOrientation =
                when (textViewAttrs.getInt(R.styleable.UiTextView_bgGdOrientation, 0)) {
                    1 -> GradientDrawable.Orientation.TR_BL
                    2 -> GradientDrawable.Orientation.RIGHT_LEFT
                    3 -> GradientDrawable.Orientation.BR_TL
                    4 -> GradientDrawable.Orientation.BOTTOM_TOP
                    5 -> GradientDrawable.Orientation.BL_TR
                    6 -> GradientDrawable.Orientation.LEFT_RIGHT
                    7 -> GradientDrawable.Orientation.TL_BR
                    else -> GradientDrawable.Orientation.TOP_BOTTOM
                }

            val gradient = textViewAttrs.getString(R.styleable.UiTextView_strokeGradient)
            if (!gradient.isNullOrEmpty()) {
                strokeGradient = gradient.split(" ").map {
                    if (it.isValidHexColor()) {
                        Color.parseColor(it)
                    } else {
                        fromColor("00ffffff")
                    }
                }.toIntArray()
            }
            strokeGradientOrientation =
                when (textViewAttrs.getInt(R.styleable.UiTextView_strokeGdOrientation, 6)) {
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
            if (isGradient) {
                updateGradient()
            } else {
                updateBackground()
            }


            textGradientOrientation =
                textViewAttrs.getInt(R.styleable.UiTextView_textGdOrientation, 0)
            tColorDark =
                textViewAttrs.getColor(R.styleable.UiTextView_tvColorDark, currentTextColor)
            tColorLight =
                textViewAttrs.getColor(R.styleable.UiTextView_tvColorLight, currentTextColor)
            tColorHint =
                textViewAttrs.getColor(R.styleable.UiTextView_tvColorHint, currentHintTextColor)
            underlineText = textViewAttrs.getBoolean(R.styleable.UiTextView_underLine, false)
            textGradientStart =
                textViewAttrs.getColor(R.styleable.UiTextView_textGradientStart, Color.TRANSPARENT)
            textGradientEnd =
                textViewAttrs.getColor(R.styleable.UiTextView_textGradientEnd, Color.TRANSPARENT)

            textGradient = textViewAttrs.getBoolean(
                R.styleable.UiTextView_textGradient,
                this@UiTextView.textGradient
            )
            textViewAttrs.recycle()
            applyStyles()
        }
    }

    fun setTextColorGradient(startColor: Int, endColor: Int, orientation: Int): UiTextView {
        this.textGradientStart = startColor
        this.textGradientEnd = endColor
        this.textGradientOrientation = orientation
        this.textGradient = true
        applyTextGradient()
        return this
    }

    fun setTextColor(tColorLight: Int, tColorDark: Int): UiTextView {
        this.tColorLight = tColorLight
        this.tColorDark = tColorDark
        updateTextColor()
        return this
    }

    private fun updateTextColor() {
        setTextColor(if (isDarkMode()) tColorDark else tColorLight)
    }


    private fun applyStyles() {
        if (textGradient) {
            applyTextGradient()
        } else {
            setTextColor(if (isDarkMode()) tColorDark else tColorLight)
        }
        if (underlineText) {
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }


    fun setBackground(bgColorLight: Int, bgColorDark: Int): UiTextView {
        this.bgColorLight = bgColorLight
        this.bgColorDark = bgColorDark
        updateBackground()
        return this
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
            cornerRadius = this@UiTextView.cornerRadius
        }
        background = backgroundDrawable
    }

    private fun updateBackground() {
        val strokeColor = if (isDarkMode()) stColorDark else stColorLight
        val backgroundColor = if (isDarkMode()) bgColorDark else bgColorLight

        val backgroundDrawable = GradientDrawable().apply {
            setColor(backgroundColor)
            cornerRadius = this@UiTextView.cornerRadius
        }
        background = backgroundDrawable
    }

    private fun applyTextGradient() {
        val gradientOrientation = when (textGradientOrientation) {
            0 -> TextGradientOrientation.TOP_TO_BOTTOM
            1 -> TextGradientOrientation.TR_BL
            2 -> TextGradientOrientation.RIGHT_TO_LEFT
            3 -> TextGradientOrientation.BR_TL
            4 -> TextGradientOrientation.BOTTOM_TO_TOP
            5 -> TextGradientOrientation.BL_TR
            6 -> TextGradientOrientation.LEFT_TO_RIGHT
            7 -> TextGradientOrientation.TL_BR
            else -> TextGradientOrientation.TOP_TO_BOTTOM
        }
        setGradient(textGradientStart, textGradientEnd, gradientOrientation)
        postInvalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas) // Draws child views first

        if (stWidth > 0) {


            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = this@UiTextView.stWidth
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
                    paint.color = if (isDarkMode()) stColorDark else stColorLight
                }
            } else {
                paint.color = if (isDarkMode()) stColorDark else stColorLight
            }
            val rectF = RectF(
                this@UiTextView.stWidth / 2,
                this@UiTextView.stWidth / 2,
                width.toFloat() - this@UiTextView.stWidth / 2,
                height.toFloat() - this@UiTextView.stWidth / 2
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
