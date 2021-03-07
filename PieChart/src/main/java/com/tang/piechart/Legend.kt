package com.tang.piechart

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView

/*图例控件*/
class Legend : RelativeLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    /*图例圆*/
    private val circle = Circle(this.context)

    /*图例文字*/
    private val text = TextView(this.context)

    init {
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, Util.dip2px(this.context, 50))
        this.layoutParams = layoutParams
        this.addView(circle)
        this.addView(text) // 初始化图例控件
        val circleLayoutParams =
            LayoutParams(Util.dip2px(this.context, 10), Util.dip2px(this.context, 10))
        circleLayoutParams.addRule(CENTER_VERTICAL)
        circleLayoutParams.setMargins(
            Util.dip2px(this.context, 5),
            Util.dip2px(this.context, 5),
            Util.dip2px(this.context, 5),
            Util.dip2px(this.context, 5)
        )
        circle.layoutParams = circleLayoutParams
        circle.id = R.id.legendCircle // 初始化圆
        val textLayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        textLayoutParams.addRule(CENTER_VERTICAL)
        textLayoutParams.addRule(RIGHT_OF, circle.id)
        textLayoutParams.setMargins(
            Util.dip2px(this.context, 5),
            Util.dip2px(this.context, 5),
            Util.dip2px(this.context, 5),
            Util.dip2px(this.context, 5)
        )
        text.layoutParams = textLayoutParams
        text.textSize = 13f
        text.id = R.id.legendText // 初始化文字
    }

    /**
     * 设置图例数据
     * @param color 图例圆颜色
     * @param textString 图例文字
     */
    fun setData(color: Int, textString: String) {
        circle.setColor(color)
        text.text = textString
    }
}