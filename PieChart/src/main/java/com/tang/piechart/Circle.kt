package com.tang.piechart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/* 自定义圆*/
class Circle : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    /* 画笔*/
    private var mPaint: Paint = Paint()

    init { // 初始化
        mPaint.isAntiAlias = true // 开启抗锯齿
        mPaint.color = Color.BLACK // 设置画笔颜色
    }

    /**
     * 设置圆颜色
     * @param c 颜色int值
     */
    fun setColor(c: Int) {
        mPaint.color = c
        invalidate() // 重绘圆
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val with = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (with > height) { // 设置控件为正方形
            height
        } else {
            with
        }
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mPaint.style = Paint.Style.FILL_AND_STROKE
        val radius = height / 2f
        canvas?.drawCircle(radius, radius, radius, mPaint) // 绘制圆
    }
}
