package  com.dong.baselibrary.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
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
import androidx.core.view.setPadding
import com.dong.baselibrary.R
import com.dong.baselibrary.base.GradientOrientation
import java.lang.Math.pow
import kotlin.math.sqrt


@SuppressLint("CustomViewStyleable")
class RoundImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val imageView: AppCompatImageView = AppCompatImageView(context, attrs, defStyleAttr)
    private val roundBorder= UiLinearLayout(context,attrs, defStyleAttr)

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

    private val isDarkMode: Boolean
        get() {
            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        }
    private var clipContent: Boolean = true
    private var strokeGradientOrientation = GradientOrientation.LEFT_TO_RIGHT
    private var strokeGradient: IntArray? = null

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

            typedArray.recycle()

            val imageViewAttrs = context.obtainStyledAttributes(it, R.styleable.RoundImageView)
            val srcImgResId = imageViewAttrs.getResourceId(R.styleable.RoundImageView_ri_src, 0)
            val scaleTypeValue = imageViewAttrs.getInt(R.styleable.RoundImageView_ri_scaleType, 3)
            val adjustViewBounds =
                imageViewAttrs.getBoolean(R.styleable.RoundImageView_android_adjustViewBounds, true)
            imageViewAttrs.recycle()


            if (isGradient) {
                updateGradient()
            } else {
                updateBackground()
            }
            if (srcImgResId != 0) {
                imageView.setImageResource(srcImgResId)
            }
            this.clipContent = true

            imageView.scaleType = parseScaleType(scaleTypeValue)
            imageView.rotation = 0f
            imageView.adjustViewBounds = adjustViewBounds
            imageView.setBackgroundColor(fromColor("00000000"))
            this@RoundImageView.setPadding(stWidth.toInt())
            val backgroundImg = GradientDrawable().apply {
                cornerRadius = this@RoundImageView.cornerRadius
            }
            this@RoundImageView.post {
                val cornerRadius = sqrt(
                    pow((width - 2 * stWidth).toDouble(), 2.0) +
                            pow((height - 2 * stWidth).toDouble(), 2.0)
                ).toFloat() / 2
                backgroundImg.cornerRadius= cornerRadius
                imageView.background = backgroundImg
            }
            addView(imageView)
            setClipContent(true)
            setWillNotDraw(false)
            roundBorder.setBgColor(transparent, transparent)
            roundBorder.setStrokeWidth(stWidth.toInt())
            roundBorder.setCornerRadius(radius = cornerRadius)
            roundBorder.setGradientStroke(strokeGradient)
            addView(roundBorder)

        }
    }


    private fun parseScaleType(value: Int): ImageView.ScaleType = when (value) {
        0 -> ImageView.ScaleType.MATRIX
        1 -> ImageView.ScaleType.FIT_XY
        2 -> ImageView.ScaleType.FIT_START
        3 -> ImageView.ScaleType.FIT_CENTER
        4 -> ImageView.ScaleType.FIT_END
        5 -> ImageView.ScaleType.CENTER
        6 -> ImageView.ScaleType.CENTER_CROP
        7 -> ImageView.ScaleType.CENTER_INSIDE
        else -> ImageView.ScaleType.FIT_CENTER
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
            cornerRadius = this@RoundImageView.cornerRadius
        }
        background = backgroundDrawable
    }

    fun setImageResource(resource: Int) {
        imageView.setImageResource(resource)
    }

    fun setImageDrawable(resource: Drawable?) {
        imageView.setImageDrawable(resource)
    }

    fun setImageBitmap(resource: Bitmap?) {
        imageView.setImageBitmap(resource)
    }

    fun setImageUri(resource: Uri?) {
        imageView.setImageURI(resource)
    }

    fun setScaleType(scaleType: ImageView.ScaleType) {
        imageView.scaleType = scaleType
    }


    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        (background as? GradientDrawable)?.cornerRadius = radius + 1.2f
    }

    fun setStrokeWidth(width: Int) {
        stWidth = width.toFloat()
        postInvalidate()
    }

    private fun updateBackground() {
        val backgroundColor = if (isDarkMode) bgColorDark else bgColorLight

        val backgroundDrawable = GradientDrawable().apply {
            setColor(backgroundColor)
            cornerRadius = this@RoundImageView.cornerRadius + 1.2f
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
        postInvalidate()
    }

    fun setBgColor(colorDark: Int, colorLight: Int) {
        bgGradientStart = colorDark
        bgGradientEnd = colorLight
        postInvalidate()
    }

    fun setClipContent(clip: Boolean) {
        clipContent = clip
        clipToPadding = clip
        clipChildren = clip
        clipToOutline = clip

    }

    fun setBgColorLight(color: Int) {
        bgGradientEnd = color
        postInvalidate()
    }
}
