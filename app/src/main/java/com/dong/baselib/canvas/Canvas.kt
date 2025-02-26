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
    thirdPoint: PointF
) {
    val path = Path().apply {
        moveTo(firstPoint.x, firstPoint.y)
        lineTo(secondPoint.x, secondPoint.y)
        lineTo(thirdPoint.x, thirdPoint.y)
        close()
    }
    drawPath(path, borderPaint)
}

fun Canvas.drawShapeWithPoints(points: List<PointF>, paint: Paint) {
    if (points.size < 2) return

    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
        close()
    }
    drawPath(path, paint)
}

fun Canvas.drawRoundedPolygon(points: List<PointF>, radius: Float, paint: Paint) {
    if (points.size < 3) return
    drawRoundedShape(points, radius, paint)
}

fun Canvas.drawRoundedShape(points: List<PointF>, radius: Float, paint: Paint) {
    if (points.size < 2) return

    val path = Path()
    val pointCount = points.size

    for (i in points.indices) {
        val current = points[i]
        val next = points[(i + 1) % pointCount]
        val previous = points[(i - 1 + pointCount) % pointCount]

        val vectorIn = PointF(current.x - previous.x, current.y - previous.y)
        val vectorOut = PointF(next.x - current.x, next.y - current.y)

        val lengthIn = vectorIn.length()
        val lengthOut = vectorOut.length()

        val scaledIn = vectorIn.scale(radius / lengthIn)
        val scaledOut = vectorOut.scale(radius / lengthOut)

        val cornerStart = PointF(current.x - scaledIn.x, current.y - scaledIn.y)
        val cornerEnd = PointF(current.x + scaledOut.x, current.y + scaledOut.y)

        if (i == 0) {
            path.moveTo(cornerStart.x, cornerStart.y)
        } else {
            path.lineTo(cornerStart.x, cornerStart.y)
        }
        path.quadTo(current.x, current.y, cornerEnd.x, cornerEnd.y)
    }

    path.close()
    drawPath(path, paint)
}

fun Canvas.drawRoundRectPath(
    rectF: RectF, radius: Float,
    topLeft: Boolean, topRight: Boolean,
    bottomLeft: Boolean, bottomRight: Boolean,
    paint: Paint
) {
    val path = Path()

    val width = rectF.width()
    val height = rectF.height()
    val halfSize = width / 2
    if (width == height && radius >= halfSize) {
        drawCircle(rectF.centerX(), rectF.centerY(), halfSize, paint)
        return
    }

    val maxRadius = minOf(width, height) / 2
    val safeRadius = radius.coerceAtMost(maxRadius)

    path.moveTo(rectF.left, rectF.bottom - if (bottomLeft) safeRadius else 0f)

    if (topLeft) {
        path.lineTo(rectF.left, rectF.top + safeRadius)
        path.quadTo(rectF.left, rectF.top, rectF.left + safeRadius, rectF.top)
    } else {
        path.lineTo(rectF.left, rectF.top)
    }

    if (topRight) {
        path.lineTo(rectF.right - safeRadius, rectF.top)
        path.quadTo(rectF.right, rectF.top, rectF.right, rectF.top + safeRadius)
    } else {
        path.lineTo(rectF.right, rectF.top)
    }

    if (bottomRight) {
        path.lineTo(rectF.right, rectF.bottom - safeRadius)
        path.quadTo(rectF.right, rectF.bottom, rectF.right - safeRadius, rectF.bottom)
    } else {
        path.lineTo(rectF.right, rectF.bottom)
    }

    if (bottomLeft) {
        path.lineTo(rectF.left + safeRadius, rectF.bottom)
        path.quadTo(rectF.left, rectF.bottom, rectF.left, rectF.bottom - safeRadius)
    } else {
        path.lineTo(rectF.left, rectF.bottom)
    }

    path.close()
    drawPath(path, paint)
}

private fun Point.toPointF(): PointF {
    return PointF(this.x.toFloat(), this.y.toFloat())
}

private fun PointF.length(): Float {
    return Math.hypot(this.x.toDouble(), this.y.toDouble()).toFloat()
}

private fun PointF.scale(scalar: Float): PointF {
    return PointF(this.x * scalar, this.y * scalar)
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
