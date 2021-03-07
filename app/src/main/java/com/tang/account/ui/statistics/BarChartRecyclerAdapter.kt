package com.tang.account.ui.statistics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tang.account.R
import com.tang.account.viewmodel.StatisticsViewModel

/*柱状图recycler适配器*/
class BarChartRecyclerAdapter(
    private val activity: StatisticsActivity,
    private val viewModel: StatisticsViewModel
) :
    RecyclerView.Adapter<BarChartRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /*金额*/
        val amountText: TextView = view.findViewById(R.id.amount_text)

        /*柱*/
        val barChartColumn: View = view.findViewById(R.id.bar_chart_column)

        /*时间*/
        val timeText: TextView = view.findViewById(R.id.time_text)

        /*整个控件*/
        val body: LinearLayout = view.findViewById(R.id.body)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.statistics_bar_chart_item, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false) // 禁用复用
        val r = viewModel.statisticsListTotal[position]
        holder.amountText.text = r.getAmount().toString() // 设置金额
        val s = r.getInfo().split(" ")
        val layoutParams = holder.barChartColumn.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = s[1].toFloat() // 设置柱高度权重
        holder.timeText.text = s[0] // 设置时间
        holder.body.setOnClickListener { // 设置控件的点击事件(点击选中)
            viewModel.selectedBarPosition.value = position
        }
        viewModel.selectedBarPosition.observe(activity, { p -> // 监听被选中位置
            if (p == position) { // 当前item被选中
                holder.barChartColumn.setBackgroundResource(R.color.selectedBarChartItemColor)
                holder.timeText.setTextColor(activity.getColor(R.color.selectedTextColor))
                holder.amountText.setTextColor(activity.getColor(R.color.selectedTextColor)) // 更改显示
                viewModel.startPartStatistics(s[2].toLong(), s[3].toLong()) // 开始进行当前时间段的详细统计
            } else {
                holder.barChartColumn.setBackgroundResource(R.color.barChartItemColor)
                holder.timeText.setTextColor(activity.getColor(R.color.normalTextColor))
                holder.amountText.setTextColor(activity.getColor(R.color.normalTextColor)) // 更改显示
            }
        })
    }

    override fun getItemCount() = viewModel.statisticsListTotal.size
}