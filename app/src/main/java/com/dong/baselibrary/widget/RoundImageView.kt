package  com.dong.baselibrary.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.setPadding
import com.dong.baselibrary.R
import com.dong.baselibrary.base.GradientOrientation


@SuppressLint("CustomViewStyleable")
class RoundImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val imageView: AppCompatImageView = AppCompatImageView(context, attrs, defStyleAttr)
    private val roundBorder = UiLinearLayout(context, attrs, defStyleAttr)
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
            val padding =
                imageViewAttrs.getDimension(R.styleable.RoundImageView_android_padding, 0f)
            imageViewAttrs.recycle()
            this.setPadding(0)
            imageView.setPadding(padding.toInt())

            if (isGradient) {
                updateGradient()
            } else {
                updateBackground()
            }

            this.clipContent = true

            imageView.scaleType = parseScaleType(scaleTypeValue)
            imageView.rotation = 0f
            imageView.adjustViewBounds = adjustViewBounds
            imageView.setBackgroundColor(fromColor("00000000"))
            val layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )

            layoutParams.setMargins(
                stWidth.toInt(),
                stWidth.toInt(),
                stWidth.toInt(),
                stWidth.toInt()
            )

            imageView.layoutParams = layoutParams
            addView(imageView)
            setClipContent(true)
            setWillNotDraw(false)
            roundBorder.setBgColor(transparent, transparent)
            roundBorder.setStrokeWidth(stWidth.toInt())
            roundBorder.setCornerRadius(radius = cornerRadius)
            roundBorder.setGradientStroke(strokeGradient)
            addView(roundBorder)
            if (srcImgResId != 0) {
                setImageResource(srcImgResId)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.outlineProvider =
                OutlineProvider(w - 2 * stWidth.toInt(), h - 2 * stWidth.toInt())
            imageView.clipToOutline = true
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class OutlineProvider(var w: Int, var h: Int) : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val mBorderRect = Rect(
                0,
                0, w, h
            )
            outline.setRoundRect(mBorderRect, cornerRadius * 0.95f)
        }
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


    fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getRoundedDrawable(bitmap: Bitmap): RoundedBitmapDrawable {
        val roundedDrawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
        return roundedDrawable
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun setImageResource(resource: Int) {
        val drawable = AppCompatResources.getDrawable(context, resource)
        drawable?.let {
            val bitmap = drawableToBitmap(it)
            imageView.setImageDrawable(getRoundedDrawable(bitmap))
        } ?: Log.e("ImageError", "Drawable resource not found: $resource")
    }

    fun setImageDrawable(resource: Drawable?) {
        resource?.let {
            val bitmap = (resource as BitmapDrawable).bitmap
            lastedBitmap = bitmap
            imageView.setImageDrawable(getRoundedDrawable(bitmap))
        }
    }

    fun setImageBitmap(resource: Bitmap?) {
        resource?.let {
            lastedBitmap = it
            imageView.setImageDrawable(getRoundedDrawable(it))
        }
    }

    private var lastedBitmap: Bitmap? = null
    fun setImageUri(resource: Uri?) {
        resource?.let {
            val bitmap = uriToBitmap(it)
            lastedBitmap = bitmap
            bitmap?.let { imageView.setImageDrawable(getRoundedDrawable(it)) }
        }
    }

    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        (background as? GradientDrawable)?.cornerRadius = radius + 1.2f
        setImageBitmap(lastedBitmap)

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