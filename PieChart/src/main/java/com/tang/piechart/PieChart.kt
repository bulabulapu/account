package com.tang.piechart

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.ListView
import android.widget.RelativeLayout

/*饼状图控件*/
class PieChart : RelativeLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    /*饼*/
    private val pie = Pie(this.context)

    /*图例list*/
    private val legendList = ListView(this.context)

    init {
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        this.layoutParams = layoutParams
        this.addView(pie)
        this.addView(legendList) // 初始化此控件
        val pieLayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        pieLayoutParams.addRule(CENTER_VERTICAL)
        pie.layoutParams = pieLayoutParams
        pie.id = R.id.pie // 初始化饼
        val legendLayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        legendLayoutParams.addRule(CENTER_VERTICAL)
        legendLayoutParams.addRule(RIGHT_OF, pie.id)
        legendLayoutParams.leftMargin = 80
        legendList.layoutParams = legendLayoutParams
        legendList.divider = null // 初始化图例,取消图例间隙横线
    }

    /**
     * 设置数据
     * @param activity 父activity
     * @param list 数据类list
     */
    fun setData(activity: Activity, list: List<PieChartElement>) {
        var sum = 0f // amount总和
        for (e in list) { // 计算总和
            sum += e.amount
        }
        for (e in list) { // 设置每块的百分比
            e.setPercentNum(Util.formatFloat2(e.amount / sum * 100))
        }
        pie.setData(list)
        legendList.adapter = LegendAdapter(activity, R.layout.legend_item, list)
    }

    /**
     * 设置背景颜色
     * @param c 颜色
     */
    fun setBgColor(c: Int) {
        pie.setBgColor(c)
    }
}