package com.tang.account.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tang.account.model.Record

/*带有多选功能的recordViewModel*/
open class CheckedRecordViewModel : ViewModel() {

    /*当前列表数据*/
    protected val recordList = mutableListOf<Record>()

    /*是否为多选状态*/
    val isCheckedStatus = MutableLiveData(false)

    /*是否全选*/
    val isCheckedAll = MutableLiveData(false)

    /*已选择的项*/
    protected val checkedList = mutableListOf<Long>()

    /*选择事件监听器*/
    val onCheckedChange = MutableLiveData(true)

    /**
     * 添加选中的record
     *
     *@param id record的id
     */
    fun addChecked(id: Long) {
        if (!hasChecked(id) && id > 0) {
            checkedList.add(id)
            onCheckedChange.value = onCheckedChange.value != true
        }
    }

    /**
     * 移除一条已选中夫人record
     *
     * @param id record的id
     */
    fun removeChecked(id: Long) {
        if (hasChecked(id)) {
            checkedList.remove(id)
            onCheckedChange.value = onCheckedChange.value != true
        }
    }

    /**
     * 查询某条record是否被选择
     *
     * @param id record的id
     * @return 选中返回true,否则false
     */
    fun hasChecked(id: Long): Boolean {
        return checkedList.contains(id)
    }

    /**
     * 清空选择的record
     */
    fun clearChecked() {
        checkedList.clear()
        onCheckedChange.value = onCheckedChange.value != true
    }

    /**
     * 已选中的record数目
     */
    fun getCheckedCount(): Int {
        return checkedList.size
    }

    /**
     * record的数目
     */
    fun getListSize() = recordList.size

    /**
     * 清空record列表
     */
    open fun clearList() = recordList.clear()

    /**
     * 向recordList中添加多个record
     *
     * @param list 待添加的list
     */
    fun addToList(list: List<Record>) = recordList.addAll(list)

    /**
     * 获取recordList数据
     */
    fun getList(): List<Record> = recordList

    /**
     * 获取recordList中某个位置的record
     *
     * @param position 位置
     * @return 该位置的record或者null
     */
    fun getRecordAt(position: Int): Record? = if (position >= 0 && position < recordList.size) {
        recordList[position]
    } else {
        null
    }

    /**
     * 通过id从当前recordList获取record详细数据
     *
     * @param id 需要获取的record的id
     * @return record或者null
     */
    fun getRecordByIdFromList(id: Long): Record? {
        for (r in recordList) {
            if (r.id == id) {
                return r
            }
        }
        return null
    }
}