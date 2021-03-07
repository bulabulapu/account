package com.tang.account.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.account.AccountApplication
import com.tang.account.logic.Repository
import com.tang.account.logic.util.OtherUtil
import com.tang.account.logic.util.RecordTransformer
import com.tang.account.logic.util.TimeUtil
import com.tang.account.model.Record
import com.tang.account.ui.statistics.StatisticsActivity
import com.tang.piechart.PieChartElement
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/*StatisticsActivity的viewModel*/
class StatisticsViewModel : ViewModel() {
    /*是否进行修改操作标志量*/
    var dataFixedStatus = 0

    /*当前界面统计标识:年报告*/
    val REPORT_YEAR = 0

    /*当前界面统计标识:月报告*/
    val REPORT_MONTH = 1

    /*当前界面统计标识:周报告*/
    val REPORT_WEEK = 2

    /*当前界面报告标识*/
    var reportFlag = -1

    /*
    * 总的统计list
    * info中含有四个字符串
    * info[0]为时间段
    * info[1]为柱高度的权重
    * info[3]为时间段起时间
    * info[4]为时间段止时间*/
    val statisticsListTotal = mutableListOf<Record>()

    /*
    *详细统计list
    * info为有色条的长度权重*/
    val statisticsListPart = mutableListOf<Record>()

    /*饼图数据*/
    val pieChartElementList = mutableListOf<PieChartElement>()

    /*临时变量,用于总统计*/
    private val tempListTotal = mutableListOf<Record>()

    /*柱状图选中位置*/
    val selectedBarPosition = MutableLiveData(-1)

    /*总统计状态量*/
    val totalStatistics = MutableLiveData<Boolean>()

    /*详细统计状态栏*/
    val partStatistics = MutableLiveData<Boolean>()

    /**
     * 开始总统计操作
     */
    private fun startTotalStatistics() {
        viewModelScope.launch {
            val timeList = mutableListOf<Long>() // 时间段列表
            val amountList = mutableListOf<Float>() // 每个时间段的金额
            statisticsListTotal.clear()
            tempListTotal.clear()
            var time = Repository.getEarliestTime() // 本地数据库中最早的记录的时间
            if (time == 0L) time = TimeUtil.getNowTime()
            timeList.addAll(
                when (reportFlag) {
                    REPORT_YEAR -> parseYears(time)
                    REPORT_MONTH -> parseMonths(time)
                    REPORT_WEEK -> parseWeeks(time)
                    else -> mutableListOf()
                }
            )
            amountList.clear()
            amountList.addAll(Repository.getExpenseAmountStatisticsByTime(timeList))
            var max = amountList.maxOrNull() // 计算高度权重
            if (max == null) max = 1000000f
            var percent: Float
            for (i in 0 until amountList.size) {
                percent = sqrt(OtherUtil.formatFloat2(amountList[i] / max))
                if (percent == 1f) percent -= 0.000001f
                percent = (100 * percent) / (1 - percent) // 权重
                tempListTotal[i].setAmount(amountList[i])
                tempListTotal[i].setInfo(
                    tempListTotal[i].getInfo() +
                            "$percent " +
                            "${timeList[i * 2]} ${timeList[i * 2 + 1]}"
                )
            }
            statisticsListTotal.addAll(tempListTotal)
            OtherUtil.alterTriggerStatus(totalStatistics) // 总统计完成
        }
    }

    /**
     * 开始详细统计
     * @param startTime 时间段起时间
     * @param endTime 时间段止时间
     */
    fun startPartStatistics(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            val l =
                Repository.getAmountStatisticsByTimeAndCategory(startTime, endTime, RecordTransformer.categoryExpenseList)
            var sum = 0f // 用于计算类别百分比
            statisticsListPart.clear()
            for (i in 0 until l.size) {
                if (l[i] > 0) { // 非0类别
                    sum += l[i]
                    statisticsListPart.add(
                        Record(
                            RecordTransformer.categoryExpenseList[i],
                            l[i],
                            0,
                            RecordTransformer.QI_TA
                        )
                    )
                }
            }
            statisticsListPart.sortWith(Record.AmountDesComparator()) // 降序排序
            pieChartElementList.clear()
            for (r in statisticsListPart) {
                var percent = OtherUtil.formatFloat2(r.getAmount() / sum)
                if (percent == 1f) percent -= 0.000001f
                r.setInfo(((100 * percent) / (1 - percent)).toString())
                pieChartElementList.add( // 添加饼图数据
                    PieChartElement(
                        AccountApplication.context.getColor(
                            RecordTransformer.getCategoryColor(r.getCategory())
                        ),
                        RecordTransformer.transform(r.getCategory()),
                        r.getAmount()
                    )
                )
            }
            OtherUtil.alterTriggerStatus(partStatistics) // 详细统计结束
        }
    }

    /**
     * 重新开始统计操作
     */
    fun restartStatistics() {
        dataFixedStatus = StatisticsActivity.OPERATION_DATA_FIXED // 当有数据更改后进行此操作
        startTotalStatistics()
    }

    /**
     * 开始统计操作
     * @param flag 统计界面标识
     */
    fun startStatistics(flag: Int) {
        reportFlag = if (flag < 0 || flag > 2) REPORT_MONTH
        else flag
        startTotalStatistics()
    }

    /**
     * 解析为年时间段
     * @param time 最早时间
     * @return 时间段list
     */
    private fun parseYears(time: Long): List<Long> {
        val list = mutableListOf<Long>() // 时间段list
        var year = TimeUtil.stampToYear(time) // 最早时间所处年份
        val nowYear = TimeUtil.stampToYear(TimeUtil.getNowTime()) // 现在年份
        while (year <= nowYear) {
            tempListTotal.add(
                Record(
                    RecordTransformer.QI_TA,
                    0f,
                    0,
                    RecordTransformer.QI_TA,
                    "$year "
                )
            )
            list.add(
                TimeUtil.getZPZStamp(
                    TimeUtil.getTimeStamp(year, 1, 1, 0, 0)
                )
            )
            year++
            list.add(
                TimeUtil.getZPZStamp(
                    TimeUtil.getTimeStamp(year, 1, 1, 0, 0)
                )
            )
        }
        return list
    }

    /**
     * 解析为月时间段
     * @param time 最早时间
     * @return 时间段list
     */
    private fun parseMonths(time: Long): List<Long> {
        val list = mutableListOf<Long>() // 时间段list
        var year = TimeUtil.stampToYear(time) // 最早时间段所处年份
        var month = TimeUtil.stampToMonth(time) // 最早时间段所处月份
        val nowYear = TimeUtil.stampToYear(TimeUtil.getNowTime()) // 现在年份
        val nowMonth = TimeUtil.stampToMonth(TimeUtil.getNowTime()) // 现在月份
        while (year < nowYear) { // 往年
            tempListTotal.add(
                Record(
                    RecordTransformer.QI_TA,
                    0f,
                    0,
                    RecordTransformer.QI_TA,
                    "${year}年${month}月 "
                )
            )
            list.add(
                TimeUtil.getZPZStamp(
                    TimeUtil.getTimeStamp(year, month, 1, 0, 0)
                )
            )
            month++
            if (month > 12) {
                year++
                month = 1
            }
            list.add(
                TimeUtil.getZPZStamp(
                    TimeUtil.getTimeStamp(year, month, 1, 0, 0)
                )
            )
        }
        while (month <= nowMonth) { // 今年
            tempListTotal.add(
                Record(
                    RecordTransformer.QI_TA,
                    0f,
                    0,
                    RecordTransformer.QI_TA,
                    "${year}年${month}月 "
                )
            )
            list.add(
                TimeUtil.getZPZStamp(
                    TimeUtil.getTimeStamp(year, month, 1, 0, 0)
                )
            )
            month++
            list.add(
                TimeUtil.getZPZStamp(
                    TimeUtil.getTimeStamp(year, month, 1, 0, 0)
                )
            )
        }
        return list
    }

    /**
     * 解析为周时间段
     * @param time 最早时间
     * @return 时间段list
     */
    private fun parseWeeks(time: Long): List<Long> {
        val list = mutableListOf<Long>() // 时间段list
        var year = TimeUtil.stampToYear(time) // 最早时间段所处年份
        val now = TimeUtil.getNowTime()
        var t =
            TimeUtil.getZPZStamp(time - TimeUtil.ONE_DAY * (TimeUtil.stampToDayWithNum(time) - 1)) // 最早时间所在周的开始时间
        var week: Int // 时间所在当年的周数
        while (t < now) {
            week = TimeUtil.stampToWeek(t)
            list.add(t)
            t += TimeUtil.ONE_DAY * 7
            list.add(t) // 添加时间段
            if (week == 1 && year + 1 == TimeUtil.stampToYear(t)) { // 跨年
                year++
            }
            tempListTotal.add( // 添加统计记录
                Record(
                    RecordTransformer.QI_TA,
                    0f,
                    0,
                    RecordTransformer.QI_TA,
                    "${year}年${week}周 "
                )
            )
        }
        return list
    }

}
