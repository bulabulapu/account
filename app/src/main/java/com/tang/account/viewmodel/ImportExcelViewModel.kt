package com.tang.account.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tang.account.logic.Repository
import com.tang.account.model.Record
import kotlinx.coroutines.launch

/*ImportFromExcelActivity的ViewModel*/
class ImportExcelViewModel : CheckedRecordViewModel() {

    /*保存完成标志*/
    val isFinishSave = MutableLiveData<Boolean>()

    /*从excel中加载的list*/
    val loadListFromExcel = MutableLiveData<Boolean>()

    init { // 初始化,默认在选择状态
        isCheckedStatus.value = true
    }

    /**
     * 从文件加载数据
     * @param uri 文件uri
     */
    fun loadRecordsFromExcel(uri: Uri) {
        viewModelScope.launch {
            val list = Repository.loadRecordsFromExcel(uri)
            if (list.isEmpty()) {
                loadListFromExcel.value = false
            } else {
                clearList()
                addToList(list)
                loadListFromExcel.value = true
            }
        }
    }

    /**
     * 将选中的record保存至数据库
     */
    fun saveCheckedRecordsToDB() {
        viewModelScope.launch {
            val checkedRecords = mutableListOf<Record>() // 已选中的record的数据列表
            for (id in checkedList) {
                getRecordByIdFromList(id)?.let { r ->
                    r.id = Record.DEFAULT_ID
                    checkedRecords.add(r)
                }
            }
            isFinishSave.value = Repository.insertRecords(checkedRecords)
        }
    }
}