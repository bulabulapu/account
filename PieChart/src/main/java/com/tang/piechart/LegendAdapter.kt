package com.tang.piechart

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

/**
 * 图例的adapter
 * @param activity 父activity
 * @param resId 图例布局文件id
 * @param data 图例数据
 */
class LegendAdapter(activity: Activity, private val resId: Int, data: List<PieChartElement>) :
    ArrayAdapter<PieChartElement>(activity, resId, data) {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resId, parent, false)
        val legend: Legend = view.findViewById(R.id.legend)
        val pieChartElement = getItem(position)
        if (pieChartElement != null) { // 设置图例数据
            legend.setData(
                pieChartElement.color,
                pieChartElement.category + ":" + pieChartElement.percent + "%"
            )
        }
        return view
    }
}