package com.tang.account.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tang.account.logic.Repository
import com.tang.account.logic.util.OtherUtil
import com.tang.account.logic.util.TimeUtil
import com.tang.account.model.Record
import com.tang.account.ui.RecordRecyclerAdapter
import kotlinx.coroutines.launch

class MainPageViewModel : CheckedRecordViewModel() {

    /*界面显示我的支出*/
    val STATUS_MY_EXPENSE = 0

    /*界面显示我的收入*/
    val STATUS_MY_INCOME = 1

    /*界面显示我的账单*/
    val STATUS_MY_EXPENSE_AND_INCOME = 2

    /*当前界面显示的状态*/
    var currentDisplayStatus = STATUS_MY_EXPENSE

    /*详情页操作的record的位置*/
    var clickedPosition: Int = 0

    /*今日累计金额*/
    var todayAmount: Float = 0f

    /*当月累计金额*/
    var monthAmount: Float = 0f

    /*异步刷新显示的位置*/
    var displayStartPosition: Int = 0

    /*加载数据完成*/
    val loadListFromDB = MutableLiveData<Boolean>()

    /*删除完成标识,内容为需要更改的位置*/
    val deleteFinished = MutableLiveData<Boolean>()

    /*金额统计完成标识*/
    val amountStatistics = MutableLiveData<Boolean>()

    /*插入完成标识*/
    val insertFinished = MutableLiveData<String>()

    init { // 初始化操作,添加头部item
        clearList()
    }

    /**
     * 刷新统计数据
     */
    fun refreshAmountStatistics() {
        viewModelScope.launch {
            val nowTime = TimeUtil.getZPZStamp(TimeUtil.getNowTime())
            val year = TimeUtil.stampToYear(nowTime)
            val month = TimeUtil.stampToMonth(nowTime)
            val monthStartTime = TimeUtil.getZPZStamp(
                TimeUtil.getTimeStamp(
                    year,
                    month,
                    1, 0, 0
                )
            ) // 当月月初时间
            val nextMonthStartTime =
                monthStartTime + TimeUtil.getTheNumOfDayInMonth(
                    year,
                    month
                ) * TimeUtil.ONE_DAY // 当月月末时间
            val result = when (currentDisplayStatus) {
                STATUS_MY_EXPENSE -> {
                    Repository.getExpenseAmountStatisticsByTime(
                        listOf(
                            nowTime,
                            nowTime + TimeUtil.ONE_DAY,
                            monthStartTime,
                            nextMonthStartTime
                        )
                    )
                }
                STATUS_MY_INCOME -> {
                    Repository.getIncomeAmountStatisticsByTime(
                        listOf(
                            nowTime,
                            nowTime + TimeUtil.ONE_DAY,
                            monthStartTime,
                            nextMonthStartTime
                        )
                    )
                }
                else -> {
                    Repository.getAmountStatisticsByTime(
                        listOf(
                            nowTime,
                            nowTime + TimeUtil.ONE_DAY,
                            monthStartTime,
                            nextMonthStartTime
                        )
                    )
                }
            }
            todayAmount = result[0]
            monthAmount = result[1]
            OtherUtil.alterTriggerStatus(amountStatistics)
        }
    }

    /**
     * 删除选中的记录
     */
    fun deleteSelectedRecords() {
        viewModelScope.launch {
            deleteFinished.value = false
            deleteFinished.value = Repository.deleteRecords(checkedList)
        }
    }

    /**
     * 删除所有记录
     */
    fun deleteAllRecords() {
        viewModelScope.launch {
            deleteFinished.value = false
            deleteFinished.value = when (currentDisplayStatus) {
                STATUS_MY_INCOME -> Repository.deleteAllIncomeRecords()
                STATUS_MY_EXPENSE -> Repository.deleteAllExpenseRecords()
                else -> Repository.deleteAllRecords()
            }
            clearChecked()
        }
    }

    /**
     * 加载[startTime,endTime)内的记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    fun loadRecords(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            addToListByOrder(
                when (currentDisplayStatus) {
                    STATUS_MY_INCOME -> Repository.loadIncomeRecordsByTime(startTime, endTime)
                    STATUS_MY_EXPENSE -> Repository.loadExpenseRecordsByTime(startTime, endTime)
                    else -> Repository.loadRecordsByTime(startTime, endTime)
                }
            )
            loadListFromDB.value = false
            OtherUtil.alterTriggerStatus(loadListFromDB)
        }
    }

    /**
     * 加载endTime前count条记录
     * @param endTime 结束时间
     * @param count 数量
     */
    fun loadRecords(endTime: Long, count: Int) {
        viewModelScope.launch {
            addToListByOrder(
                when (currentDisplayStatus) {
                    STATUS_MY_INCOME -> Repository.loadIncomeRecordsByTimeAndCount(endTime, count)
                    STATUS_MY_EXPENSE -> Repository.loadExpenseRecordsByTimeAndCount(
                        endTime,
                        count
                    )
                    else -> Repository.loadRecordsByTimeAndCount(endTime, count)
                }
            )
            loadListFromDB.value = false
            OtherUtil.alterTriggerStatus(loadListFromDB)
        }
    }

    /**
     * 加载数据库中所有数据
     */
    fun loadAllRecords() {
        viewModelScope.launch {
            addToListByOrder(
                when (currentDisplayStatus) {
                    STATUS_MY_INCOME -> Repository.loadAllIncomeRecords()
                    STATUS_MY_EXPENSE -> Repository.loadAllExpenseRecords()
                    else -> Repository.loadAllRecords()
                }
            )
            loadListFromDB.value = false
            OtherUtil.alterTriggerStatus(loadListFromDB)
        }
    }

    /**
     * 从recordList中移除操作位置的record
     */
    fun removeClickedRecord() {
        recordList.removeAt(clickedPosition)
    }

    /**
     * 更改操作位置的record
     * @param record 新的record
     */
    fun alterRecord(record: Record) {
        recordList[clickedPosition] = record
    }

    /**
     * 向recordList(当前数据list)中插入一条数据,并排序
     * @param record 插入的数据
     */
    fun insertRecord(record: Record) {
        var str: String
        var position = 0 // 由高到低排列,当前record应该所处的位置
        var i = 0
        while (i < getListSize()) { // 找到当前record的位置
            if (record.getTime() > getTimeByPosition(i)) {
                position = i
                break
            }
            i++
        }
        if (i == getListSize()) { // 当前列表为空或者位置在最后
            position = i
        }
        recordList.add(position, record)
        str = "$position" // 插入record并添加更改显示位置
        if (position == 0 || TimeUtil.getZPZStamp(getTimeByPosition(position - 1)) !=
            TimeUtil.getZPZStamp(record.getTime())
        ) { // 检查是否需要插入日期类型item
            val r = Record( // 时间设置为当天的最晚时间(最大值)
                "",
                0f,
                TimeUtil.getZPZStamp(record.getTime()) + TimeUtil.ONE_DAY - 1,
                ""
            )
            r.id = RecordRecyclerAdapter.ID_DATE
            recordList.add(position, r)
            str = "$str 2" // 插入日期类型item并显示
        } else {
            str = "$str 1"
        }
        insertFinished.value = str
    }

    /**
     * 根据position获取list中的record的时间
     */
    fun getTimeByPosition(position: Int) = recordList[position].getTime()

    /**
     * 根据position获取list中的record的id
     */
    fun getIdByPosition(position: Int) = recordList[position].id

    /**
     * 设置头部统计控件显示的数据
     * @param today 今日累计
     * @param month 本月累计
     */
    fun setHeaderParam(today: Float, month: Float) {
        recordList[0].setAmount(OtherUtil.formatFloat2(today))
        recordList[0].setInfo(OtherUtil.formatFloat2(month).toString())
    }

    /**
     * 清空当前数据列表
     */
    override fun clearList() { // 保存头部的item
        super.clearList()
        val r = Record(
            "",
            0f,
            TimeUtil.getNowTime() + TimeUtil.ONE_DAY,
            "",
            "0"
        )
        r.id = RecordRecyclerAdapter.ID_HEAD
        recordList.add(r)
    }

    /**
     * 将list有序添加到recordList中
     * @param list 待添加的list
     */
    private fun addToListByOrder(list: List<Record>) {
        displayStartPosition = getListSize()
        if (list.isNotEmpty()) {
            var lastEmptyRecordPosition = 0 // 已有列表中最后一个日期类型的item位置
            if (getListSize() != 0) { // 确定这个日期类型item位置
                for (i in getListSize() - 1 downTo 0) {
                    if (getIdByPosition(i) == RecordRecyclerAdapter.ID_DATE) {
                        lastEmptyRecordPosition = i
                        break
                    }
                }
            }
            addToList(list)
            var previousTime: Long  // 最后一个日期item的时间
            previousTime = if (lastEmptyRecordPosition != 0) {
                getTimeByPosition(lastEmptyRecordPosition)
            } else { // 原列表中没有数据,找不到日期类型item
                TimeUtil.getZPZStamp(getTimeByPosition(1)) + 2 * TimeUtil.ONE_DAY - 1
            }
            lastEmptyRecordPosition++
            var i = lastEmptyRecordPosition
            while (i < getListSize()) { // 从下一个item开始匹配时间
                val time = getTimeByPosition(i)
                if (time < previousTime - TimeUtil.ONE_DAY) { // i位置的记录时间不在当天,就在i位置插入一个日期item
                    previousTime = TimeUtil.getZPZStamp(time) + TimeUtil.ONE_DAY - 1
                    val r = Record(
                        "",
                        0f,
                        previousTime,
                        ""
                    )
                    r.id = RecordRecyclerAdapter.ID_DATE
                    recordList.add(i, r)
                }
                i++
            }
        }
    }
}