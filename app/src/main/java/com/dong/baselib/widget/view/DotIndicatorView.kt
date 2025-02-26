package  com.dong.baselib.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.dong.baselib.widget.gdEnd
import com.dong.baselib.widget.gdStart

class DotIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dotCount: Int = 3
    private var currentPage: Int = 0

    private val inactiveDotSize = 8.dpToPx()
    private val activeDotWidth = (1.2f * inactiveDotSize).dpToPx()
    private val dotHeight = inactiveDotSize
    private val dotSpacing = 8.dpToPx()

    private val activeDotColor = Color.parseColor("#2473BD")
    private val inactiveDotColor = Color.parseColor("#737373")

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        dotCount = 3
        currentPage = 0
    }
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var currentX = 0f
        for (i in 0 until dotCount) {
            val dotWidth = if (i == currentPage) activeDotWidth else inactiveDotSize
            val gradient = LinearGradient(
                currentX,
                (height / 2).toFloat() + dotHeight / 2,
                currentX + dotWidth,
                (height / 2).toFloat() - dotHeight / 2,
                intArrayOf(gdStart, gdEnd),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )

            if(i==currentPage)  {
                paint.shader = gradient
            }else{
                paint.shader= LinearGradient(
                    currentX,
                    (height / 2).toFloat() + dotHeight / 2,
                    currentX + dotWidth,
                    (height / 2).toFloat() - dotHeight / 2,
                    intArrayOf(inactiveDotColor, inactiveDotColor),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
            }

            val cx = currentX + dotWidth / 2
            canvas.drawRoundRect(
                cx - dotWidth / 2,
                (height / 2).toFloat() - dotHeight / 2,
                cx + dotWidth / 2,
                (height / 2).toFloat() + dotHeight / 2,
                dotHeight / 2f,
                dotHeight / 2f,
                paint
            )

            
            paint.shader = null

            currentX += dotWidth + dotSpacing
        }
    }


    fun setDotCount(count: Int) {
        dotCount = count
        invalidate()
    }

    fun setCurrentPage(page: Int) {
        currentPage = page
        invalidate()
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun Float.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalWidth = (dotCount - 1) * (inactiveDotSize + dotSpacing) + activeDotWidth
        val desiredHeight = dotHeight
        val width = resolveSize(totalWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    fun attachToViewPager(viewPager: ViewPager) {
        viewPager.adapter?.let {
            setDotCount(it.count)
        }
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                setCurrentPage(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    fun attachToViewPager2(viewPager2: ViewPager2) {
        viewPager2.adapter?.let {
            setDotCount(it.itemCount)
        }
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentPage(position)
            }
        })
    }
}
