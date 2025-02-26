package  com.dong.baselib.widget.view

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import  com.dong.baselib.R
import com.dong.baselib.listener.OnStateChangeListener


class CustomSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var trackColorDarkOff: Int = fromColor("#B3C3D6")
    private var trackColorLightOff: Int = fromColor("#B3C3D6")
    private var thumbColorDarkOff: Int = fromColor("#FFFFFF")
    private var thumbColorLightOff: Int = fromColor("#FFFFFF")
    private var trackColorDarkOn: Int =  fromColor("#FD4F84")
    private var trackColorLightOn: Int = fromColor("#FD4F84")
    private var thumbColorDarkOn: Int = fromColor("#FFFFFF")
    private var thumbColorLightOn: Int = fromColor("#FFFFFF")
    private var stateTrack: Boolean = false

    private val paintTrack = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val paintThumb = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private var thumbPosition: Float = 0f

    private val isDarkMode: Boolean
        get() {
            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TrackerStyle,
            0, 0
        ).apply {
            try {
                trackColorDarkOff =
                    getColor(R.styleable.TrackerStyle_track_dark_off, trackColorDarkOff)
                trackColorLightOff =
                    getColor(R.styleable.TrackerStyle_track_light_off, trackColorLightOff)
                thumbColorDarkOff =
                    getColor(R.styleable.TrackerStyle_thumbColorDarkOff, thumbColorDarkOff)
                thumbColorLightOff =
                    getColor(R.styleable.TrackerStyle_thumbColorLightOff, thumbColorLightOff)
                trackColorDarkOn =
                    getColor(R.styleable.TrackerStyle_trackColorDarkOn, trackColorDarkOn)
                trackColorLightOn =
                    getColor(R.styleable.TrackerStyle_trackColorLightOn, trackColorLightOn)
                thumbColorDarkOn =
                    getColor(R.styleable.TrackerStyle_thumbColorDarkOn, thumbColorDarkOn)
                thumbColorLightOn =
                    getColor(R.styleable.TrackerStyle_thumbColorLightOn, thumbColorLightOn)
                stateTrack = getBoolean(R.styleable.TrackerStyle_stateTrack, false)
            } finally {
                recycle()
            }
        }
        updateColors()
        thumbPosition = if (stateTrack) 1f else 0f
    }

    private fun updateColors() {
        if (stateTrack) {
            paintTrack.color = if (isDarkMode) trackColorDarkOn else trackColorLightOn
            paintThumb.color = if (isDarkMode) thumbColorDarkOn else thumbColorLightOn
        } else {
            paintTrack.color = if (isDarkMode) trackColorDarkOff else trackColorLightOff
            paintThumb.color = if (isDarkMode) thumbColorDarkOff else thumbColorLightOff
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
        updateColors()
        onStateChangeListener?.onStateChanged(stateTrack)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (!isDisable) {
                toggleState()
            }

        }
        return true
    }

    fun setState(isOn: Boolean) {
        stateTrack = isOn
        thumbPosition = if (isOn) 1f else 0f
        updateColors()
        invalidate()
    }

    private var isDisable = false
    fun isDisable(isOn: Boolean = false) {
        this.isDisable = isOn
        invalidate()
    }

    private var onStateChangeListener: OnStateChangeListener? = null



    fun onStateChangeListener(state: OnStateChangeListener) {
        this.onStateChangeListener = state
        invalidate()
    }

    private fun fromColor(colorString: String): Int {
        return android.graphics.Color.parseColor(colorString)
    }
}

