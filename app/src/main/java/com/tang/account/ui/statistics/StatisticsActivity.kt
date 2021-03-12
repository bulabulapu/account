package com.tang.account.ui.statistics

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.tang.account.AccountApplication
import com.tang.account.MyAppCompatActivity
import com.tang.account.R
import com.tang.account.databinding.ActivityStatisticsBinding
import com.tang.account.ui.statistics.categorydetail.CategoryDetailActivity
import com.tang.account.viewmodel.StatisticsViewModel

/*统计界面*/
class StatisticsActivity : MyAppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(StatisticsViewModel::class.java) }
    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var barChartRecyclerAdapter: BarChartRecyclerAdapter
    private lateinit var statisticsRecyclerAdapter: StatisticsRecyclerAdapter

    companion object {
        /*数据被修改状态量*/
        const val OPERATION_DATA_FIXED = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backButton.setOnClickListener { onBackPressed() } // 返回按钮事件
        binding.switchButton.setOnClickListener { // 切换按钮事件
            binding.barChartRecycler.scrollToPosition(0)
            viewModel.selectedBarPosition.value = -1
            when (viewModel.reportFlag) {
                viewModel.REPORT_WEEK -> {
                    viewModel.startStatistics(viewModel.REPORT_MONTH)
                    binding.titleText.setText(R.string.month_report)
                }
                viewModel.REPORT_MONTH -> {
                    viewModel.startStatistics(viewModel.REPORT_YEAR)
                    binding.titleText.setText(R.string.year_report)
                }
                viewModel.REPORT_YEAR -> {
                    viewModel.startStatistics(viewModel.REPORT_WEEK)
                    binding.titleText.setText(R.string.week_report)
                }
            }
        }

        val barLayoutManager = LinearLayoutManager(this)
        barLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.barChartRecycler.layoutManager = barLayoutManager
        barChartRecyclerAdapter = BarChartRecyclerAdapter(this, viewModel)
        binding.barChartRecycler.adapter = barChartRecyclerAdapter // barRecycler初始化

        binding.pieChart.setBgColor(getColor(R.color.defaultBgColor)) // 饼图背景色

        val layoutManager = LinearLayoutManager(this)
        binding.statisticsRecycler.layoutManager = layoutManager
        statisticsRecyclerAdapter = StatisticsRecyclerAdapter(this, viewModel)
        binding.statisticsRecycler.adapter = statisticsRecyclerAdapter // 类别比重recycler初始化

        viewModel.totalStatistics.observe(this, { // 总统计结束,更改显示,并开始详细统计
            barChartRecyclerAdapter.notifyDataSetChanged()
            if (viewModel.selectedBarPosition.value == -1) {
                viewModel.selectedBarPosition.value = viewModel.statisticsListTotal.size - 1
            }
            binding.barChartRecycler.scrollToPosition(
                viewModel.selectedBarPosition.value ?: viewModel.statisticsListTotal.size - 1
            )
        })
        viewModel.partStatistics.observe(this, { // 详细统计结束,更改显示,设置饼图数据
            binding.pieChart.setData(this, viewModel.pieChartElementList)
            statisticsRecyclerAdapter.notifyDataSetChanged()
        })
        if (viewModel.reportFlag == -1) { // 当此activity不为重建时,进行统计操作
            viewModel.startStatistics(viewModel.REPORT_MONTH)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AccountApplication.REQUEST_CATEGORY_DETAIL && resultCode == Activity.RESULT_OK) {
            if (data?.getIntExtra(
                    "operation",
                    -1
                ) == CategoryDetailActivity.OPERATION_DATA_FIXED
            ) { // 当类别详细界面有数据操作时,重新统计
                viewModel.restartStatistics()
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("operation", viewModel.dataFixedStatus)
        setResult(Activity.RESULT_OK, intent) // 将数据修改操作状态量传回
        finish()
    }
}