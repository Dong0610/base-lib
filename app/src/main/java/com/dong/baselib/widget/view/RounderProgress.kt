package  com.dong.baselib.widget.view;

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import  com.dong.baselib.R


class RounderProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var defBackground: Int = Color.GRAY
    private var defTint: Int = Color.BLUE
    private var maxVal: Float = 100.0f
    private var minVal: Float = 0.0f
    private var currentVal: Float = 50.0f
    private var isAutoAnimation: Boolean = false
    private var isGradient: Boolean = false
    private var startColor: Int = Color.parseColor("#2196F3")
    private var endColor: Int = Color.parseColor("#9C27B0")
    private var timeAnim: Int = 5
    private var progressAnimator: ValueAnimator? = null

    private var progressListener: OnProgressListener? = null
    private var isRestart: Boolean = false

    init {
        init()
        attrs?.let {
            getTypedArr(context, it, defStyleAttr)
        }
    }

    private fun init() {
        defBackground = Color.GRAY
        defTint = Color.BLUE
        maxVal = 100.0f
        minVal = 0.0f
        currentVal = 50.0f
        isAutoAnimation = false
    }

    private fun getTypedArr(context: Context, attrs: AttributeSet, defStyleAttr: Int) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ProgressCustom,
            defStyleAttr,
            0
        ).apply {
            try {
                defBackground =
                    getColor(R.styleable.ProgressCustom_progressBackground, defBackground)
                defTint = getColor(R.styleable.ProgressCustom_progressTint, defTint)
                maxVal = getFloat(R.styleable.ProgressCustom_maxValue, maxVal)
                minVal = getFloat(R.styleable.ProgressCustom_minValue, minVal)
                currentVal = getFloat(R.styleable.ProgressCustom_currentValue, currentVal)
                isAutoAnimation =
                    getBoolean(R.styleable.ProgressCustom_autoAnimate, isAutoAnimation)
                isGradient = getBoolean(R.styleable.ProgressCustom_gradient, isGradient)
                startColor = getColor(R.styleable.ProgressCustom_gradientStart, startColor)
                endColor = getColor(R.styleable.ProgressCustom_gradientEnd, endColor)
            } finally {
                recycle()
            }
        }
    }

    interface OnProgressListener {
        fun onEnd()
        fun onProgress(progress: Float)
    }

    fun progressEvent(event: OnProgressListener): RounderProgress {
        this.progressListener = event
        invalidate()
        return this
    }

    fun restartAnim(restart: Boolean): RounderProgress {
        this.isRestart = restart
        invalidate()
        return this
    }

    fun startAutoAnimation() {
        progressAnimator?.takeIf { it.isRunning }?.cancel()
        progressAnimator = ValueAnimator.ofFloat(minVal, maxVal).apply {
            duration = (timeAnim * 1000).toLong()
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                if (!isRestart && calculateProgressPercentage() > 100f) {
                    cancel()
                    progressListener?.onEnd()
                }
                setCurrentValue(animatedValue)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (progressListener != null && !progressAnimator!!.isRunning) {
                        progressListener?.onEnd()
                    }
                }
            })
            if (isRestart) {
                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressAnimator?.cancel()
    }

    fun endAnimate(): RounderProgress {
        progressAnimator?.cancel()
        progressListener?.onEnd()
        return this
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawProgress(canvas)
    }

    private fun drawProgress(canvas: Canvas) {
        val backgroundPaint = Paint().apply {
            color = defBackground
            style = Paint.Style.FILL
        }
        drawRoundRectPath(
            canvas,
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            (height / 2).toFloat(),
            backgroundPaint
        )

        val progressPaint = Paint().apply {
            if (isGradient) {
                shader = LinearGradient(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    startColor,
                    endColor,
                    Shader.TileMode.CLAMP
                )
            } else {
                color = defTint
            }
            style = Paint.Style.FILL
        }

        val progressWidth = calculateProgressWidth()
        val progressPercentage = calculateProgressPercentage()

        if (currentVal != 0f) {
            progressListener?.onProgress(progressPercentage)
            drawRoundRectPath(
                canvas,
                RectF(0f, 0f, progressWidth, height.toFloat()),
                (height / 2).toFloat(),
                progressPaint
            )
        }
    }

    private fun drawRoundRectPath(canvas: Canvas, rectF: RectF, radius: Float, paint: Paint) {

        canvas.drawRoundRect(rectF, radius, radius, paint)
    }

    private fun calculateProgressWidth(): Float {
        val percentage = (currentVal - minVal) / (maxVal - minVal)
        return width * percentage
    }

    private fun calculateProgressPercentage(): Float {
        return (currentVal - minVal) / (maxVal - minVal) * 100
    }

    fun timeAnim(seconds: Int): RounderProgress {
        this.timeAnim = seconds
        invalidate()
        return this
    }

    fun gradientTint(startColor: Int, endColor: Int): RounderProgress {
        isGradient = true
        this.startColor = startColor
        this.endColor = endColor
        invalidate()
        return this
    }

    fun setMaxValue(maxValue: Float): RounderProgress {
        this.maxVal = maxValue
        invalidate()
        return this
    }

    fun setMinValue(minValue: Float): RounderProgress {
        this.minVal = minValue
        invalidate()
        return this
    }

    fun setCurrentValue(currentValue: Float): RounderProgress {
        this.currentVal = currentValue
        invalidate()
        return this
    }
}