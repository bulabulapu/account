package com.tang.account.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.account.logic.Repository
import com.tang.account.logic.util.OtherUtil
import com.tang.account.model.Record
import kotlinx.coroutines.launch

/*DetailActivity的ViewModel*/
class DetailViewModel : ViewModel() {

    /*当前record*/
    private lateinit var record: Record

    /*当前record的详细参数*/
    val category = MutableLiveData<String>()
    val amount = MutableLiveData<Float>()
    val time = MutableLiveData<Long>()
    val way = MutableLiveData<String>()
    val info = MutableLiveData<String>()

    /*保存完成*/
    val isSaved = MutableLiveData<Boolean>()

    /**
     * ViewModel初始化(设置record)
     */
    fun setData(r: Record) {
        record = r
        category.value = r.getCategory()
        amount.value = r.getAmount()
        time.value = r.getTime()
        way.value = r.getWay()
        info.value = r.getInfo()
    }

    /**
     * 返回当前的record
     */
    fun getData(): Record = record

    /**
     * 将单独的详细参数变量全部写入record,并保存到数据库
     */
    fun save() {
        viewModelScope.launch {
            category.value?.let { record.setCategory(it) }
            amount.value?.let { record.setAmount(it) }
            time.value?.let { record.setTime(it) }
            way.value?.let { record.setWay(it) }
            info.value?.let { record.setInfo(it) }
            if (record.id == Record.DEFAULT_ID) { // 为新增记录
                record.id = Repository.insertRecord(record)
            } else { // 修改记录
                Repository.updateRecord(record)
            }
            OtherUtil.alterTriggerStatus(isSaved)
        }
    }

    /**
     * 从数据库中删除该record
     */
    fun delete() {
        if (record.id != Record.DEFAULT_ID) {
            Repository.deleteRecord(record.id)
        }
    }

    /**
     * 设置详细参数:类别
     * @param category 新的类别
     */
    fun setCategory(category: String) {
        this.category.value = category
    }

    /**
     * 设置详细参数:金额
     * @param amount 新的金额
     */
    fun setAmount(amount: Float) {
        this.amount.value = amount
    }

    /**
     * 设置详细参数:时间
     * @param time 新的时间
     */
    fun setTime(time: Long) {
        this.time.value = time
    }

    /**
     * 设置详细参数:方式
     * @param way 新的方式
     */
    fun setWay(way: String) {
        this.way.value = way
    }

    /**
     * 设置详细参数:详情
     * @param info 新的详情
     */
    fun setInfo(info: String) {
        this.info.value = info
    }
}