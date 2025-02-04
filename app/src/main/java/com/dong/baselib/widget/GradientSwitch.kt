package com.dong.baselib.widget

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.dong.baselib.R


class GradientSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var trackColorOff: IntArray = intArrayOf(fromColor("#7F7F7F"), fromColor("#7F7F7F"))
    private var thumbColorOff: IntArray = intArrayOf(fromColor("#FFFFFF"), fromColor("#FFFFFF"))
    private var trackColorOn: IntArray = intArrayOf(fromColor("#FFFFFF"), fromColor("#FFFFFF"))
    private var thumbColorOn: IntArray = intArrayOf(fromColor("#FFFFFF"), fromColor("#FFFFFF"))
    private var stateTrack: Boolean = false

    private val paintTrack = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val paintThumb = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private var thumbPosition: Float = 0f

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GradientSwitch,
            defStyleAttr, 0
        ).apply {
            try {

                val trackColorOffString = getString(R.styleable.GradientSwitch_trackColorOff)
                if (!trackColorOffString.isNullOrEmpty()) {
                    trackColorOff = trackColorOffString.split(" ").map {
                        if (it.isValidHexColor()) {
                            Color.parseColor(it)
                        } else {
                            fromColor("#00FFFFFF")
                        }
                    }.toIntArray()
                }
                val thumbColorOffString = getString(R.styleable.GradientSwitch_thumbColorOff)
                if (!thumbColorOffString.isNullOrEmpty()) {
                    thumbColorOff = thumbColorOffString.split(" ").map {
                        if (it.isValidHexColor()) {
                            Color.parseColor(it)
                        } else {
                            fromColor("#FFFFFF") // Default to white if invalid color
                        }
                    }.toIntArray()
                }

                // Similar handling for other color attributes (on-state colors)
                val trackColorOnString = getString(R.styleable.GradientSwitch_trackColorOn)
                if (!trackColorOnString.isNullOrEmpty()) {
                    trackColorOn = trackColorOnString.split(" ").map {
                        if (it.isValidHexColor()) {
                            Color.parseColor(it)
                        } else {
                            fromColor("#FFFFFF")
                        }
                    }.toIntArray()
                }

                val thumbColorOnString = getString(R.styleable.GradientSwitch_thumbColorOn)
                if (!thumbColorOnString.isNullOrEmpty()) {
                    thumbColorOn = thumbColorOnString.split(" ").map {
                        if (it.isValidHexColor()) {
                            Color.parseColor(it)
                        } else {
                            fromColor("#FFFFFF")
                        }
                    }.toIntArray()
                }

                stateTrack = getBoolean(R.styleable.GradientSwitch_gd_stateTrack, false)
            } finally {
                recycle()
            }
        }
        updateColors()
        thumbPosition = if (stateTrack) 1f else 0f
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun updateColors() {
        val trackColorAnimator = ObjectAnimator.ofInt(
            this, "trackColor", trackColorOff[0], trackColorOn[0]
        )
        trackColorAnimator.setEvaluator(ArgbEvaluator())
        trackColorAnimator.duration = 500 // Adjust duration as needed
        trackColorAnimator.start()

        val thumbColorAnimator = ObjectAnimator.ofInt(
            this, "thumbColor", thumbColorOff[0], thumbColorOn[0]
        )
        thumbColorAnimator.setEvaluator(ArgbEvaluator())
        thumbColorAnimator.duration = 500 // Adjust duration as needed
        thumbColorAnimator.start()
        if (stateTrack) {
            paintTrack.shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f,
                trackColorOn, // Array of colors
                FloatArray(trackColorOn.size) { it.toFloat() / (trackColorOn.size - 1) },
                Shader.TileMode.CLAMP
            )
            paintThumb.shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f,
                thumbColorOn,
                FloatArray(thumbColorOn.size) { it.toFloat() / (thumbColorOn.size - 1) },
                Shader.TileMode.CLAMP
            )
        } else {
            paintTrack.shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f,
                trackColorOff, // Array of colors
                FloatArray(trackColorOff.size) { it.toFloat() / (trackColorOff.size - 1) },
                Shader.TileMode.CLAMP
            )
            paintThumb.shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f,
                thumbColorOff,
                FloatArray(thumbColorOff.size) { it.toFloat() / (thumbColorOff.size - 1) },
                Shader.TileMode.CLAMP
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rectF, height / 2f, height / 2f, paintTrack)

        val thumbRadius = (height * 0.7f) / 2f
        var thumbX = thumbPosition * if (stateTrack) {
            width - thumbRadius - height * 0.15f
        } else {
            thumbRadius + height * 0.25f
        }
        val thumbY = height / 2f
        if (thumbX < thumbY) {
            thumbX = thumbY
        }
        canvas.drawCircle(thumbX, thumbY, thumbRadius, paintThumb)
    }

    private fun toggleState() {
        val startValue = if (stateTrack) 1f else 0f
        val endValue = if (stateTrack) 0f else 1f

        ValueAnimator.ofFloat(startValue, endValue).apply {
            duration = 300
            addUpdateListener { animation ->
                thumbPosition = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
        stateTrack = !stateTrack
        updateColors() // Update colors after the toggle
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && !isDisabled) {
            toggleState()
        }
        return true
    }

    fun setState(isOn: Boolean) {
        stateTrack = isOn
        thumbPosition = if (isOn) 1f else 0f
        updateColors()
        invalidate()
    }

    private var isDisabled = false
    fun setDisabled(disabled: Boolean) {
        this.isDisabled = disabled
        invalidate()
    }

    private fun fromColor(colorString: String): Int {
        return Color.parseColor(colorString)
    }

    // Utility function to validate if a string is a valid hex color
    private fun String.isValidHexColor(): Boolean {
        return this.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$".toRegex())
    }
}
