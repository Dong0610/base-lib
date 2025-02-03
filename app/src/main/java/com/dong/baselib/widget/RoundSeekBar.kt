package com.dong.baselib.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.dong.baselib.R
import kotlin.math.roundToInt


interface OnSeekBarChange{
    fun onPreChange(value:Float){}
    fun onProgressChange(value:Float){}
    fun onPostChange(value:Float){}
}


class RoundSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var seekBackground: IntArray = intArrayOf( fromColor("#EAF6FD"),fromColor("#EAF6FD"))
    var seekGradient = intArrayOf(fromColor("#3F81E4"), fromColor("#112FF2"))

    var minValue: Float = 0.0f
        set(value) {
            field = value
            invalidate()
        }
    var maxValue: Float = 100.0f
        set(value) {
            field = value
            invalidate()
        }

    private var onSeekChange: ((Float) -> Unit)? = null
    private var onProgressChange: OnSeekBarChange? = null

    fun listenValueChanged(ps: OnSeekBarChange?){
        this.onProgressChange= ps
        invalidate()
    }
    var currentValue: Float = 50.0f
        set(value) {
            field = value.coerceIn(minValue, maxValue)
            onSeekChange?.invoke(currentValue)
            invalidate()
        }


    var stepValue: Float = 1.0f
        set(value) {
            field = value
            invalidate()
        }

    private var width = 0f
    private var height = 0f
    private var pointCenter = PointF(0f, 0f)
    private var isDragging = false

    init {
        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.RoundSeekBar, defStyleAttr, 0)

            val sekBg = typedArray.getString(R.styleable.RoundSeekBar_seekBackground)
            if (!sekBg.isNullOrEmpty()) {
                seekBackground = sekBg.split(" ").map {
                    if (it.isValidHexColor()) {
                        Color.parseColor(it)
                    } else {
                        fromColor("#EAF6FD")
                    }
                }.toIntArray()
            }
            val sekPrs = typedArray.getString(R.styleable.RoundSeekBar_seekBackground)
            if (!sekPrs.isNullOrEmpty()) {
                seekGradient = sekPrs.split(" ").map {
                    if (it.isValidHexColor()) {
                        Color.parseColor(it)
                    } else {
                        fromColor("#3F81E4")
                    }
                }.toIntArray()
            }

            minValue = typedArray.getFloat(R.styleable.RoundSeekBar_sk_minValue, minValue)
            maxValue = typedArray.getFloat(R.styleable.RoundSeekBar_sk_maxValue, maxValue)
            currentValue =
                typedArray.getFloat(R.styleable.RoundSeekBar_sk_currentValue, currentValue)
            stepValue = typedArray.getFloat(R.styleable.RoundSeekBar_sk_stepValue, stepValue)
            typedArray.recycle()
        }
        updateThumbPosition()
        onSeekChange?.invoke(currentValue)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w.toFloat()
        height = h.toFloat()
        updateThumbPosition()
    }

    private fun updateThumbPosition() {
        pointCenter.x = ((currentValue - minValue) / (maxValue - minValue) * (width - 20))
        if (pointCenter.x < 10f) {
            pointCenter.x = 10f
        }
        pointCenter.y = height / 2f
    }


    fun onValueChange(onSeekChange: ((Float) -> Unit)) {
        this.onSeekChange = onSeekChange
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val bgHeight = height * 0.375f
        canvas.drawRoundRect(
            RectF(10f, height * 0.25f, width - 10, height * 0.75f),
            bgHeight / 2f,
            bgHeight / 2f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, width, 0f,
                    seekBackground,null,
                    Shader.TileMode.CLAMP
                )
                style = Paint.Style.FILL
            }
        )
        canvas.drawRoundRect(
            RectF(10f, height * 0.25f, pointCenter.x, height * 0.75f),
            bgHeight / 2f,
            bgHeight / 2f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, width, 0f,
                    seekGradient,null,
                    Shader.TileMode.CLAMP
                )
                style = Paint.Style.FILL
            }
        )

        canvas.drawCircle(
            pointCenter.x,
            pointCenter.y,
            (height - 4f) / 2f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = fromColor("#3F81E4")
                style = Paint.Style.FILL
            })
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onProgressChange?.onPreChange(currentValue)
                performClick()
                parent.requestDisallowInterceptTouchEvent(true)
                isDragging = true
            }

            MotionEvent.ACTION_MOVE -> {
                updateProgress(event.x)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onProgressChange?.onPostChange(currentValue)
                parent.requestDisallowInterceptTouchEvent(false)
                isDragging = false

            }
        }
        invalidate()
        return true
    }

    private fun updateProgress(x: Float) {
        val newValue = ((x / (width - 20f)) * (maxValue - minValue)) + minValue
        currentValue = roundToStep(newValue)
        updateThumbPosition()
        onProgressChange?.onProgressChange(currentValue)
        onSeekChange?.invoke(currentValue)
    }

    private fun roundToStep(value: Float): Float {
        return ((value / stepValue).roundToInt() * stepValue).coerceIn(minValue, maxValue)
    }

    private fun fromColor(colorStr: String): Int {
        return Color.parseColor(colorStr)
    }
}
