package com.dong.baselib.widget.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import com.dong.baselib.R
import com.dong.baselib.widget.fromColor

class RippleBackground @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_RIPPLE_COUNT = 6
        private const val DEFAULT_DURATION_TIME = 4000
        private const val DEFAULT_SCALE = 6.0f
        private const val DEFAULT_FILL_TYPE = 0
    }

    private var rippleColor: Int = fromColor("ff0202")
    private var rippleStrokeWidth: Float = 6f
    private var rippleRadius: Float = 64f
    private var rippleDurationTime: Int = DEFAULT_DURATION_TIME
    private var rippleAmount: Int = 0
    private var rippleDelay: Int = 0
    private var rippleScale: Float = 0f
    private var rippleType: Int = 0
    private val paint: Paint
    private var animationRunning = false
    private lateinit var animatorSet: AnimatorSet
    private var autoRun = false
    private val animatorList = mutableListOf<Animator>()
    private lateinit var rippleParams: LayoutParams
    private val rippleViewList = mutableListOf<RippleView>()

    init {

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleBackground)
            rippleColor = typedArray.getColor(R.styleable.RippleBackground_rb_color, rippleColor)
            rippleStrokeWidth = typedArray.getDimension(
                R.styleable.RippleBackground_rb_strokeWidth,
                rippleStrokeWidth
            )
            rippleRadius =
                typedArray.getDimension(R.styleable.RippleBackground_rb_radius, rippleRadius)
            rippleDurationTime =
                typedArray.getInt(R.styleable.RippleBackground_rb_duration, DEFAULT_DURATION_TIME)
            rippleAmount = typedArray.getInt(
                R.styleable.RippleBackground_rb_rippleAmount,
                DEFAULT_RIPPLE_COUNT
            )
            rippleScale = typedArray.getFloat(R.styleable.RippleBackground_rb_scale, DEFAULT_SCALE)
            autoRun = typedArray.getBoolean(R.styleable.RippleBackground_auto_run, false)
            rippleType = typedArray.getInt(R.styleable.RippleBackground_rb_type, DEFAULT_FILL_TYPE)
            typedArray.recycle()
        }

        rippleDelay = rippleDurationTime / rippleAmount

        paint = Paint().apply {
            isAntiAlias = true
            if (rippleType == DEFAULT_FILL_TYPE) {
                rippleStrokeWidth = 0f
                style = Paint.Style.FILL
            } else {
                style = Paint.Style.STROKE
            }
            color = rippleColor
        }

        rippleParams = LayoutParams(
            (2 * (rippleRadius + rippleStrokeWidth)).toInt(),
            (2 * (rippleRadius + rippleStrokeWidth)).toInt()
        ).apply {
            addRule(CENTER_IN_PARENT, TRUE)
        }

        animatorSet = AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
        }

        for (i in 0 until rippleAmount) {
            val rippleView = RippleView(context)
            addView(rippleView, rippleParams)
            rippleViewList.add(rippleView)

            val scaleXAnimator =
                ObjectAnimator.ofFloat(rippleView, "ScaleX", 1.0f, rippleScale).apply {
                    repeatCount = ObjectAnimator.INFINITE
                    repeatMode = ObjectAnimator.RESTART
                    startDelay = (i * rippleDelay).toLong()
                    duration = rippleDurationTime.toLong()
                }
            animatorList.add(scaleXAnimator)

            val scaleYAnimator =
                ObjectAnimator.ofFloat(rippleView, "ScaleY", 1.0f, rippleScale).apply {
                    repeatCount = ObjectAnimator.INFINITE
                    repeatMode = ObjectAnimator.RESTART
                    startDelay = (i * rippleDelay).toLong()
                    duration = rippleDurationTime.toLong()
                }
            animatorList.add(scaleYAnimator)

            val alphaAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", 1.0f, 0f).apply {
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.RESTART
                startDelay = (i * rippleDelay).toLong()
                duration = (rippleDurationTime).toLong()
            }
            animatorList.add(alphaAnimator)
        }

        animatorSet.playTogether(animatorList)

        if (autoRun) {
            startRippleAnimation()
        }
    }

    private inner class RippleView(context: Context) : View(context) {
        init {
            visibility = INVISIBLE
        }

        override fun onDraw(canvas: Canvas) {
            val radius = Math.min(width, height) / 2
            canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius - rippleStrokeWidth, paint)
        }
    }

    fun startRippleAnimation() {
        if (!isRippleAnimationRunning()) {
            rippleViewList.forEach { it.visibility = VISIBLE }
            animatorSet.start()
            animationRunning = true
        }
    }

    fun stopRippleAnimation() {
        if (isRippleAnimationRunning()) {
            animatorSet.end()
            animationRunning = false
        }
    }

    fun isRippleAnimationRunning(): Boolean = animationRunning
}
