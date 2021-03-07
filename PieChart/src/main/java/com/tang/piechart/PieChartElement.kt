package com.tang.piechart

/* 饼状图数据类*/
data class PieChartElement(
    /*块颜色*/
    val color: Int,
    /*类型*/
    val category: String,
    /*金额*/
    val amount: Float,
    /*此块所占比例*/
    var percent: Float = 0f
) {
    /**
     * 设置此块所占比例
     * @param p 比例百分数
     */
    fun setPercentNum(p: Float) {
        percent = p
    }
}