package com.tang.piechart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*饼*/
class Pie : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    /*画笔*/
    private var mPaint: Paint = Paint()

    /*饼所在的矩形*/
    private var mRect: RectF = RectF()

    /*每块的颜色*/
    private val colorList = mutableListOf<Int>()

    /*每块扇形的角度*/
    private val angleList = mutableListOf<Float>()

    /*块数目*/
    private var itemCount = 0

    /*背景颜色*/
    private var bgColor = Color.WHITE

    init { // 初始化
        mPaint.isAntiAlias = true // 抗锯齿
        mPaint.isDither = true // 防抖动
        mPaint.textSize = 30f // 设置文字大小
    }

    /**
     * 设置饼数据
     * @param list  数据类list
     */
    fun setData(list: List<PieChartElement>) {
        itemCount = 0
        colorList.clear()
        angleList.clear()
        for (e in list) {
            colorList.add(e.color) // 设置颜色
            angleList.add(e.percent * 3.6f) // 设置角度
            itemCount++
        }
        invalidate()
    }

    /**
     * 设置背景颜色
     * @param c 颜色
     */
    fun setBgColor(c: Int) {
        bgColor = c
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        val withMode = MeasureSpec.getMode(widthMeasureSpec)
        val withSize = MeasureSpec.getSize(widthMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val minWidth = if (withSize > heightSize) { // 设置饼为正方形
            heightSize
        } else {
            withSize
        }
        setMeasuredDimension(minWidth, minWidth)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mPaint.style = Paint.Style.FILL_AND_STROKE
        /*饼半径*/
        val radius = width / 2.5f
        /*绘制起点位置角度*/
        var startAngle = 0f
        /*饼间隙宽度*/
        val lineWidth = 3f
        /*控件中心点x坐标*/
        val centerX = width / 2f
        /*控件中心点y坐标*/
        val centerY = width / 2f
        /*间隙端点与中心点的x轴偏移*/
        var xOffset: Float
        /*间隙端点与中心点的y轴偏移*/
        var yOffset: Float
        mPaint.color = bgColor
        canvas?.drawRect(0f, 0f, width.toFloat(), width.toFloat(), mPaint) // 绘制背景
        mRect.set(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius
        )
        for (i in 0 until itemCount) { // 绘制每个扇形
            mPaint.color = colorList[i]
            val angle = angleList[i]
            canvas?.drawArc(mRect, startAngle, angle, true, mPaint)
            startAngle += angle
        }
        mPaint.color = bgColor
        mPaint.strokeWidth = lineWidth
        startAngle = 0f
        for (i in 0 until itemCount) { // 绘制间隙
            val angle = angleList[i]
            startAngle += angle
            xOffset = ((radius + 5) * cos(startAngle / 180 * PI)).toFloat()
            yOffset = ((radius + 5) * sin(startAngle / 180 * PI)).toFloat() // 计算间隙端点
            canvas?.drawLine(centerX, centerY, centerX + xOffset, centerY + yOffset, mPaint)
        }
        canvas?.drawCircle( // 绘制饼的内圆
            centerX,
            centerY,
            radius / 2f,
            mPaint
        )
//        canvas?.drawText(
//            String.format("%.2f", percent) + "%",
//            (width / 2 + radius * cos(sweepAngle * Math.PI / 360) / 4).toFloat(),
//            (width / 2 - radius * sin(sweepAngle * Math.PI / 360) / 3).toFloat(),
//            mPaint
//        )
//        canvas?.drawText(
//            String.format("%.2f", 1 - percent) + "%",
//            (width / 2 - radius * cos(sweepAngle * Math.PI / 360)).toFloat(),
//            (width / 2 + radius * sin(sweepAngle * Math.PI / 360) / 3).toFloat(),
//            mPaint
//        )
    }
}