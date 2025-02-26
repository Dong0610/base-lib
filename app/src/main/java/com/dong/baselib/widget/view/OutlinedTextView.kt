package com.dong.baselib.widget.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
class OutlinedTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val strokeWidth: Float = 8f
    private val extraHorizontalPadding: Int = 24  // Increased horizontal padding

    init {
        updatePadding()
    }

    private fun updatePadding() {
        val basePadding = (strokeWidth / 2).toInt()
        setPadding(
            basePadding + extraHorizontalPadding,  // start
            basePadding,                           // top
            basePadding + extraHorizontalPadding,  // end
            basePadding                            // bottom
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Expand measured width to accommodate the stroke
        val extraWidth = (strokeWidth * 2).toInt()
        val extraHeight = (strokeWidth * 2).toInt()

        setMeasuredDimension(
            measuredWidth + extraWidth,
            measuredHeight + extraHeight
        )
    }

    override fun onDraw(canvas: Canvas) {
        val textColor = textColors.defaultColor  // Get original text color

        // Draw outline first
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        setTextColor(Color.BLACK)
        super.onDraw(canvas)

        // Draw the original text on top
        paint.style = Paint.Style.FILL
        setTextColor(textColor)
        super.onDraw(canvas)
    }
}
