package com.tang.account.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tang.account.logic.Repository
import com.tang.account.logic.util.TimeUtil
import com.tang.account.model.Record
import kotlinx.coroutines.launch

/*ExportToExcelActivity的ViewModel*/
class ExportExcelViewModel : CheckedRecordViewModel() {

    /*导出的excel文件名*/
    private val fileName =
        "账单_" + TimeUtil.stampToDateForExcel(TimeUtil.getNowTime()) + "_" + TimeUtil.stampToTimeForExcel(
            TimeUtil.getNowTime()
        ) + TimeUtil.stampToSecond(TimeUtil.getNowTime()) + ".xlsx"

    /*加载list完成状态*/
    val loadListFromDB = MutableLiveData<Boolean>()

    /*导出结束标志*/
    val isFinishExport = MutableLiveData<Boolean>()

    init { // 初始化,默认在选择状态
        isCheckedStatus.value = true
    }

    /**
     * 加载数据
     */
    fun loadRecords() {
        viewModelScope.launch {
            val list = Repository.loadAllRecords()
            if (list.isEmpty()) {
                loadListFromDB.value = false
            } else {
                clearList()
                addToList(list)
                loadListFromDB.value = true
            }
        }
    }

    /**
     * 导出数据
     */
    fun exportRecords() {
        viewModelScope.launch {
            val checkedRecords = mutableListOf<Record>() // 已选中的record的数据列表
            if (getListSize() == getCheckedCount()) {
                checkedRecords.addAll(recordList)
            } else {
                for (id in checkedList) {
                    getRecordByIdFromList(id)?.let { r -> checkedRecords.add(r) }
                }
            }
            isFinishExport.value = Repository.exportRecordsToExcel(fileName, checkedRecords)
        }
    }
}