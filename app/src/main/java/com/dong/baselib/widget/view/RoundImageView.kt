package  com.dong.baselib.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.setPadding
import com.dong.baselib.R
import com.dong.baselib.widget.GradientOrientation
import com.dong.baselib.widget.fromColor
import com.dong.baselib.widget.isValidHexColor
import com.dong.baselib.widget.layout.UiLinearLayout
import com.dong.baselib.widget.transparent

import kotlin.math.min


@SuppressLint("CustomViewStyleable")
class RoundImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

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

    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        isDither=true
    }
    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        isDither=true
    }

    private val isDarkMode: Boolean
        get() {
            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.RoundImageView, 0, 0).apply {
            try {
                cornerRadius = getDimension(R.styleable.RoundImageView_cornerRadius, 0f)
                stWidth = getDimension(R.styleable.RoundImageView_strokeWidth, 0f)
                stColorDark = getColor(R.styleable.RoundImageView_stColorDark, Color.BLACK)
                stColorLight = getColor(R.styleable.RoundImageView_stColorLight, Color.BLACK)
                bgColorDark = getColor(R.styleable.RoundImageView_bgColorDark, Color.TRANSPARENT)
                bgColorLight = getColor(R.styleable.RoundImageView_bgColorLight, Color.TRANSPARENT)
                bgGradientStart =
                    getColor(R.styleable.RoundImageView_bgGradientStart, Color.TRANSPARENT)
                bgGradientCenter =
                    getColor(R.styleable.RoundImageView_bgGradientCenter, Color.TRANSPARENT)
                bgGradientEnd =
                    getColor(R.styleable.RoundImageView_bgGradientEnd, Color.TRANSPARENT)

                isGradient =
                    bgGradientStart != Color.TRANSPARENT && bgGradientEnd != Color.TRANSPARENT

                gradientOrientation = when (getInt(R.styleable.RoundImageView_bgGdOrientation, 0)) {
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
                    when (getInt(R.styleable.RoundImageView_strokeGdOrientation, 6)) {
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

                val gradient = getString(R.styleable.RoundImageView_strokeGradient)
                strokeGradient = gradient?.split(" ")?.map {
                    Color.parseColor(it)
                }?.toIntArray()
            } finally {
                recycle()
            }
        }
    }

    private val path = Path()

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val inset = stWidth / 2
        val minSize = minOf(viewWidth / 2, viewHeight / 2)
        val corner = min(cornerRadius, minSize)
        path.reset()
        path.addRoundRect(RectF(0f, 0f, viewWidth, viewHeight), corner, corner, Path.Direction.CW)
        canvas.clipPath(path)
        if (!isGradient) {
            paintBackground.color = if (isDarkMode) bgColorDark else bgColorLight
        } else {
            val (x0, y0, x1, y1) = when (strokeGradientOrientation) {
                GradientOrientation.TOP_TO_BOTTOM -> arrayOf(0f, 0f, 0f, viewHeight)
                GradientOrientation.BOTTOM_TO_TOP -> arrayOf(0f, viewHeight, 0f, 0f)
                GradientOrientation.LEFT_TO_RIGHT -> arrayOf(0f, 0f, viewWidth, 0f)
                GradientOrientation.RIGHT_TO_LEFT -> arrayOf(viewWidth, 0f, 0f, 0f)
                GradientOrientation.TL_BR -> arrayOf(0f, 0f, viewWidth, viewHeight)
                GradientOrientation.TR_BL -> arrayOf(viewWidth, 0f, 0f, viewHeight)
                GradientOrientation.BL_TR -> arrayOf(0f, viewHeight, viewWidth, 0f)
                GradientOrientation.BR_TL -> arrayOf(viewWidth, viewHeight, 0f, 0f)
            }
            val gradientArr = if (bgGradientCenter == fromColor("#00000000")) intArrayOf(
                bgGradientStart,
                bgGradientEnd
            ) else intArrayOf(bgGradientStart, bgGradientCenter, bgGradientEnd)
            val gradient = LinearGradient(x0, y0, x1, y1, gradientArr, null, Shader.TileMode.CLAMP)
            paintBackground.shader = gradient
        }
        canvas.drawRoundRect(RectF(0f, 0f, viewWidth, viewHeight), corner, corner, paintBackground)
        super.onDraw(canvas)
        if (stWidth > 0) {
            paintBorder.style = Paint.Style.STROKE
            paintBorder.strokeWidth = stWidth
            paintBorder.isAntiAlias = true
            paintBorder.isDither = true
            paintBorder.strokeJoin = Paint.Join.ROUND

            if (strokeGradient != null && strokeGradient!!.size > 1) {
                val (x0, y0, x1, y1) = when (strokeGradientOrientation) {
                    GradientOrientation.TOP_TO_BOTTOM -> arrayOf(0f, 0f, 0f, viewHeight)
                    GradientOrientation.BOTTOM_TO_TOP -> arrayOf(0f, viewHeight, 0f, 0f)
                    GradientOrientation.LEFT_TO_RIGHT -> arrayOf(0f, 0f, viewWidth, 0f)
                    GradientOrientation.RIGHT_TO_LEFT -> arrayOf(viewWidth, 0f, 0f, 0f)
                    GradientOrientation.TL_BR -> arrayOf(0f, 0f, viewWidth, viewHeight)
                    GradientOrientation.TR_BL -> arrayOf(viewWidth, 0f, 0f, viewHeight)
                    GradientOrientation.BL_TR -> arrayOf(0f, viewHeight, viewWidth, 0f)
                    GradientOrientation.BR_TL -> arrayOf(viewWidth, viewHeight, 0f, 0f)
                }
                val gradient = LinearGradient(
                    x0, y0, x1, y1,
                    strokeGradient!!,
                    null,
                    Shader.TileMode.CLAMP
                )
                paintBorder.shader = gradient
            } else {
                paintBorder.color = if (isDarkMode) stColorDark else stColorLight
            }
            canvas.drawRoundRect(
                RectF(inset, inset, viewWidth - inset, viewHeight - inset),
                corner,
                corner,
                paintBorder
            )
        }
    }

    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        invalidate()
    }

    fun setStrokeWidth(width: Int) {
        stWidth = width.toFloat()
        invalidate()
    }


    fun setBgColor(colorDark: Int, colorLight: Int) {
        bgColorDark = colorDark
        bgColorLight = colorLight
        invalidate()
    }

    fun setStrokeColor(colorDark: Int, colorLight: Int) {
        stColorDark = colorDark
        stColorLight = colorLight
        invalidate()
    }
}
