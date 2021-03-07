package com.tang.piechart

import android.content.Context

/* 工具单例类*/
object Util {

    /**
     * dip转换为px
     * @param dpValue dip值
     * @return px值
     */
    fun dip2px(context: Context, dpValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 格式化数值(四舍五入保留两位小数)
     * @param input 需要格式化的数值
     * @return 格式化后的数值
     */
    fun formatFloat2(input: Float): Float {
        var a: Int = (input * 1000).toInt()
        if (a % 10 >= 5) {
            a += 10
        }
        a /= 10
        return a.toFloat() / 100
    }
}