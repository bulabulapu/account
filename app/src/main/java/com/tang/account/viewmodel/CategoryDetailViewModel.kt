package com.tang.account.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tang.account.logic.Repository
import com.tang.account.logic.util.OtherUtil
import com.tang.account.logic.util.RecordTransformer
import com.tang.account.model.Record
import com.tang.account.ui.statistics.categorydetail.CategoryDetailActivity
import kotlinx.coroutines.launch

/*CategoryDetailActivity的ViewModel*/
class CategoryDetailViewModel : CheckedRecordViewModel() {

    /*当前list中记录的起时间*/
    private var startTime: Long = 0

    /*当前list中记录的止时间*/
    private var endTime: Long = 0

    /*activity的记录类型*/
    var category: String = RecordTransformer.QI_TA

    /*以时间为序标志量*/
    val ORDER_TIME = 0

    /*以金额为序标志量*/
    val ORDER_AMOUNT = 1

    /*当前排序规则*/
    var orderStatus = ORDER_TIME

    /*是否进行修改操作标志量*/
    var dataFixedStatus = 0

    /*排序完成*/
    val sortFinished = MutableLiveData<Boolean>()

    /**
     * 加载数据
     * @param start 起时间
     * @param end 止时间
     * @param cate 类型
     */
    fun loadRecords(start: Long, end: Long, cate: String) {
        startTime = start
        endTime = end
        category = cate
        loadRecordsFromDB()
    }

    /**
     * 重新加载record(当对record进行数据操作后调用)
     */
    fun reloadRecords() {
        dataFixedStatus = CategoryDetailActivity.OPERATION_DATA_FIXED
        loadRecordsFromDB()
    }

    /**
     * 从数据库中加载记录,并对加载的数据进行处理
     */
    private fun loadRecordsFromDB() {
        viewModelScope.launch {
            clearList()
            addToList(Repository.loadRecordsByTimeAndCategory(startTime, endTime, category))
            when (orderStatus) { // 对加载的record进行排序
                ORDER_TIME -> setOrderByTime()
                ORDER_AMOUNT -> setOrderByAmount()
            }
        }
    }

    /**
     * 按照金额对record降序排列
     */
    fun setOrderByAmount() {
        recordList.sortWith(Record.AmountDesComparator())
        OtherUtil.alterTriggerStatus(sortFinished)
    }

    /**
     * 按照时间对record由晚到早排序
     */
    fun setOrderByTime() {
        recordList.sortWith(Record.TimeDesComparator())
        OtherUtil.alterTriggerStatus(sortFinished)
    }
}