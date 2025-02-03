package com.dong.baselib.canvas

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout


class NativeCanvasView constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var drawAction: (Canvas.() -> Unit)? = null

    fun setDrawAction(action: Canvas.() -> Unit) {
        drawAction = action
        invalidate() // Request redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawAction?.invoke(canvas)
    }
}
fun ViewGroup.nativeCanvas(canvasAction: Canvas.() -> Unit) {
    val nativeCanvasView = NativeCanvasView(this.context).apply {
        layoutParams = when (this@nativeCanvas) {
            is LinearLayout -> LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            is FrameLayout -> FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            is RelativeLayout -> RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            is ConstraintLayout -> ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            else -> ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setDrawAction(canvasAction)
        elevation=1000f
    }

    this.post {
        if (nativeCanvasView.parent == null) {
            this.addView(nativeCanvasView)
        }
    }
}



fun Canvas.drawTriangle(
    borderPaint: Paint,
    firstPoint: PointF,
    secondPoint: PointF,
    threePoint: PointF
) {
    val shapePath = Path()
    shapePath.moveTo(firstPoint.x, firstPoint.y)
    shapePath.lineTo(secondPoint.x, secondPoint.y)
    shapePath.lineTo(threePoint.x, threePoint.y)
    shapePath.close()
    drawPath(shapePath, borderPaint)
}

fun Canvas.drawShapeWithPoints(
    points: List<PointF>,
    paint: Paint
) {
    if (points.size < 2) return

    val path = Path()
    path.moveTo(points[0].x, points[0].y)


    for (i in 1 until points.size) {
        path.lineTo(points[i].x, points[i].y)
    }

    path.close()
    drawPath(path, paint)
}

fun Canvas.drawRoundedPolygon(
    points: List<PointF>,
    radius: Float,
    paint: Paint
) {
    if (points.size < 3) return

    val path = Path()

    val pointCount = points.size
    for (i in points.indices) {
        val currentPoint = points[i]
        val nextPoint = points[(i + 1) % pointCount]
        val previousPoint = points[(i - 1 + pointCount) % pointCount]


        val vectorIn = PointF(
            currentPoint.x - previousPoint.x,
            currentPoint.y - previousPoint.y
        )
        val vectorOut = PointF(
            nextPoint.x - currentPoint.x,
            nextPoint.y - currentPoint.y
        )


        val lengthIn = Math.hypot(vectorIn.x.toDouble(), vectorIn.y.toDouble()).toFloat()
        val lengthOut = Math.hypot(vectorOut.x.toDouble(), vectorOut.y.toDouble()).toFloat()

        val scaledIn = PointF(
            vectorIn.x / lengthIn * radius,
            vectorIn.y / lengthIn * radius
        )
        val scaledOut = PointF(
            vectorOut.x / lengthOut * radius,
            vectorOut.y / lengthOut * radius
        )


        val cornerStart = PointF(
            currentPoint.x - scaledIn.x,
            currentPoint.y - scaledIn.y
        )
        val cornerEnd = PointF(
            currentPoint.x + scaledOut.x,
            currentPoint.y + scaledOut.y
        )


        if (i == 0) {
            path.moveTo(cornerStart.x, cornerStart.y)
        } else {
            path.lineTo(cornerStart.x, cornerStart.y)
        }
        path.quadTo(currentPoint.x, currentPoint.y, cornerEnd.x, cornerEnd.y)
    }

    path.close()
    drawPath(path, paint)
}


fun Canvas.drawRoundedShape(
    points: List<PointF>,
    radius: Float,
    paint: Paint
) {
    if (points.size < 2) return

    val path = Path()


    val pointCount = points.size
    for (i in points.indices) {
        val currentPoint = points[i]
        val nextPoint = points[(i + 1) % pointCount]
        val previousPoint = points[(i - 1 + pointCount) % pointCount]


        val vectorIn = PointF(
            currentPoint.x - previousPoint.x,
            currentPoint.y - previousPoint.y
        )
        val vectorOut = PointF(
            nextPoint.x - currentPoint.x,
            nextPoint.y - currentPoint.y
        )


        val lengthIn = Math.hypot(vectorIn.x.toDouble(), vectorIn.y.toDouble()).toFloat()
        val lengthOut = Math.hypot(vectorOut.x.toDouble(), vectorOut.y.toDouble()).toFloat()

        val scaledIn = PointF(
            vectorIn.x / lengthIn * radius,
            vectorIn.y / lengthIn * radius
        )
        val scaledOut = PointF(
            vectorOut.x / lengthOut * radius,
            vectorOut.y / lengthOut * radius
        )


        val cornerStart = PointF(
            currentPoint.x - scaledIn.x,
            currentPoint.y - scaledIn.y
        )
        val cornerEnd = PointF(
            currentPoint.x + scaledOut.x,
            currentPoint.y + scaledOut.y
        )


        if (i == 0) {
            path.moveTo(cornerStart.x, cornerStart.y)
        } else {
            path.lineTo(cornerStart.x, cornerStart.y)
        }
        path.quadTo(currentPoint.x, currentPoint.y, cornerEnd.x, cornerEnd.y)
    }

    path.close()
    drawPath(path, paint)
}


fun Canvas.drawRoundRectPath(
    rectF: RectF,
    radius: Float,
    topLeft: Boolean,
    topRight: Boolean,
    bottomLeft: Boolean,
    bottomRight: Boolean,
    paint: Paint
) {
    val path = Path()
    if (bottomRight) {
        path.moveTo(rectF.left, rectF.bottom - radius)
    } else {
        path.moveTo(rectF.left, rectF.bottom)
    }
    if (topLeft) {
        path.lineTo(rectF.left, rectF.top + radius)
        path.quadTo(rectF.left, rectF.top, rectF.left + radius, rectF.top)
    } else {
        path.lineTo(rectF.left, rectF.top)
    }
    if (topRight) {
        path.lineTo(rectF.right - radius, rectF.top)
        path.quadTo(rectF.right, rectF.top, rectF.right, rectF.top + radius)
    } else {
        path.lineTo(rectF.right, rectF.top)
    }
    if (bottomRight) {
        path.lineTo(rectF.right, rectF.bottom - radius)
        path.quadTo(rectF.right, rectF.bottom, rectF.right - radius, rectF.bottom)
    } else {
        path.lineTo(rectF.right, rectF.bottom)
    }
    if (bottomLeft) {
        path.lineTo(rectF.left + radius, rectF.bottom)
        path.quadTo(rectF.left, rectF.bottom, rectF.left, rectF.bottom - radius)
    } else {
        path.lineTo(rectF.left, rectF.bottom)
    }
    path.close()

    drawPath(path, paint)
}


fun getBitmapFromDrawable(context: Context, drawableResId: Int): Bitmap {
    return BitmapFactory.decodeResource(context.resources, drawableResId)
}

fun android.graphics.drawable.Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

fun Canvas.drawTriangle(
    borderPaint: Paint,
    firstPoint: Point,
    secondPoint: Point,
    threePoint: Point
) {
    val shapePath = Path()
    shapePath.moveTo(firstPoint.x.toFloat(), firstPoint.y.toFloat())
    shapePath.lineTo(secondPoint.x.toFloat(), secondPoint.y.toFloat())
    shapePath.lineTo(threePoint.x.toFloat(), threePoint.y.toFloat())
    shapePath.close()
    drawPath(shapePath, borderPaint)
}
