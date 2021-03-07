package com.tang.account.logic

import android.net.Uri
import com.tang.account.AccountApplication
import com.tang.account.logic.database.RecordDatabase
import com.tang.account.model.Record
import com.tang.account.logic.util.ExcelUtil
import com.tang.account.logic.util.MiBakUtil
import com.tang.account.logic.util.RecordTransformer
import com.tang.account.logic.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

/*数据操作单例类*/
object Repository {
    /*本地数据库DAO*/
    private var recordDao = RecordDatabase.getDatabase(AccountApplication.context).recordDao()

    /**
     * 新增一条记录
     * @param record 新增的记录
     * @return 新增记录的id值
     */
    suspend fun insertRecord(record: Record) = withContext(Dispatchers.IO) {
        recordDao.insertRecord(record)
    }

    /**
     * 新增多条记录
     * @param list 新增的record列表
     * @return 插入结束返回true
     */
    suspend fun insertRecords(list: List<Record>) = withContext(Dispatchers.IO) {
        list.forEach { r ->
            recordDao.insertRecord(r)
        }
        true
    }

    /**
     * 通过id值删除一条记录
     * @param id 被删除记录的id值
     */
    fun deleteRecord(id: Long) {
        thread { recordDao.deleteRecordById(id) }
    }

    /**
     * 通过id列表删除多条记录
     * @param ids id列表
     * @return 删除结束返回true
     */
    suspend fun deleteRecords(ids: List<Long>) = withContext(Dispatchers.IO) {
        ids.forEach { id ->
            recordDao.deleteRecordById(id)
        }
        true
    }

    /**
     * 清空数据库
     * @return 清空完成返回true
     */
    suspend fun deleteAllRecords() = withContext(Dispatchers.IO) {
        recordDao.deleteRecords(RecordTransformer.categoryList)
        true
    }

    /**
     * 删除所有支出记录
     * @return 删除完成返回true
     */
    suspend fun deleteAllExpenseRecords() = withContext(Dispatchers.IO) {
        recordDao.deleteRecords(RecordTransformer.categoryExpenseList)
        true
    }

    /**
     * 删除所有收入记录
     * @return 完成返回true
     */
    suspend fun deleteAllIncomeRecords() = withContext(Dispatchers.IO) {
        recordDao.deleteRecords(RecordTransformer.categoryIncomeList)
        true
    }

    /**
     * 加载所有记录
     * @return record列表
     */
    suspend fun loadAllRecords() = withContext(Dispatchers.IO) {
        recordDao.loadRecordsByTimeAndCategory(
            recordDao.getEarliestTime(),
            TimeUtil.getNowTime(),
            RecordTransformer.categoryList
        )
    }

    /**
     * 加载所有支出记录
     * @return record列表
     */
    suspend fun loadAllExpenseRecords() = withContext(Dispatchers.IO) {
        recordDao.loadRecordsByTimeAndCategory(
            recordDao.getEarliestTime(),
            TimeUtil.getNowTime(),
            RecordTransformer.categoryExpenseList
        )
    }

    /**
     * 加载所有收入记录
     * @return record列表
     */
    suspend fun loadAllIncomeRecords() = withContext(Dispatchers.IO) {
        recordDao.loadRecordsByTimeAndCategory(
            recordDao.getEarliestTime(),
            TimeUtil.getNowTime(),
            RecordTransformer.categoryIncomeList
        )
    }

    /**
     * 加载[startTime,endTime)内的记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return record列表
     */
    suspend fun loadRecordsByTime(startTime: Long, endTime: Long) = withContext(Dispatchers.IO) {
        recordDao.loadRecordsByTimeAndCategory(startTime, endTime, RecordTransformer.categoryList)
    }

    /**
     * 加载[startTime,endTime)内的支出记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return record列表
     */
    suspend fun loadExpenseRecordsByTime(startTime: Long, endTime: Long) =
        withContext(Dispatchers.IO) {
            recordDao.loadRecordsByTimeAndCategory(
                startTime,
                endTime,
                RecordTransformer.categoryExpenseList
            )
        }

    /**
     * 加载[startTime,endTime)内的收入记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return record列表
     */
    suspend fun loadIncomeRecordsByTime(startTime: Long, endTime: Long) =
        withContext(Dispatchers.IO) {
            recordDao.loadRecordsByTimeAndCategory(
                startTime,
                endTime,
                RecordTransformer.categoryIncomeList
            )
        }

    /**
     * 加载endTime前count条记录,不包括endTime
     * @param endTime 结束时间
     * @param count 数量
     * @return record列表
     */
    suspend fun loadRecordsByTimeAndCount(endTime: Long, count: Int) = withContext(Dispatchers.IO) {
        recordDao.loadRecordsByTimeAndCount(endTime, count)
    }

    /**
     * 加载endTime前count条支出记录,不包括endTime
     * @param endTime 结束时间
     * @param count 数量
     * @return record列表
     */
    suspend fun loadExpenseRecordsByTimeAndCount(endTime: Long, count: Int) =
        withContext(Dispatchers.IO) {
            recordDao.loadRecordsByTimeAndCount(endTime, count)
                .filter { RecordTransformer.categoryExpenseList.contains(it.getCategory()) }
        }

    /**
     * 加载endTime前count条收入记录,不包括endTime
     * @param endTime 结束时间
     * @param count 数量
     * @return record列表
     */
    suspend fun loadIncomeRecordsByTimeAndCount(endTime: Long, count: Int) =
        withContext(Dispatchers.IO) {
            recordDao.loadRecordsByTimeAndCount(endTime, count)
                .filter { RecordTransformer.categoryIncomeList.contains(it.getCategory()) }
        }

    /**
     * 加载[startTime,endTime)内的类别为category的记录,加载结束后返回数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param category 类别
     * @return record列表
     */
    suspend fun loadRecordsByTimeAndCategory(startTime: Long, endTime: Long, category: String) =
        withContext(Dispatchers.IO) {
            recordDao.loadRecordsByTimeAndCategory(startTime, endTime, listOf(category))
        }

    /**
     * 更改一条记录
     * @param newRecord 更新后的record
     */
    fun updateRecord(newRecord: Record) {
        thread { recordDao.updateRecord(newRecord) }
    }

    /**
     * 从excel文件中加载record列表
     * @param uri excel文件uri
     * @return record列表
     */
    suspend fun loadRecordsFromExcel(uri: Uri) = withContext(Dispatchers.IO) {
        ExcelUtil.readExcel(uri)
    }

    /**
     * 将record导出到excel中
     * @param name excel文件名
     * @param list 导出的record数据列表
     * @return 导出结束后返回true
     */
    suspend fun exportRecordsToExcel(name: String, list: List<Record>) =
        withContext(Dispatchers.IO) {
            ExcelUtil.writeExcel(name, list)
        }

    /**
     * 从MIUI备份文件中加载record列表
     * @param uri 文件uri
     * @return record列表
     */
    suspend fun loadRecordsFromMiBak(uri: Uri) = withContext(Dispatchers.IO) {
        MiBakUtil.readMiBak(uri)
    }

    /**
     * 统计多个时间段内的amount和(收入减去支出)
     * @param time 时间参数,为偶数个,每(n/2,n/2+1)为一对
     * @return 统计结果的list,Float类型
     */
    suspend fun getAmountStatisticsByTime(time: List<Long>) = withContext(Dispatchers.IO) {
        val list = mutableListOf<Float>()
        val incomeList = getIncomeAmountStatisticsByTime(time)
        val expenseList = getExpenseAmountStatisticsByTime(time)
        for (i in 0 until incomeList.size) {
            list.add(incomeList[i] - expenseList[i])
        }
        list
    }

    /**
     * 统计多个时间段内的支出amount和
     * @param time 时间参数,为偶数个,每(n/2,n/2+1)为一对
     * @return 统计结果的list,Float类型
     */
    suspend fun getExpenseAmountStatisticsByTime(time: List<Long>) = withContext(Dispatchers.IO) {
        val list = mutableListOf<Float>()
        for (i in 0 until time.size / 2) {
            list.add(
                recordDao.getAmountStatisticsByTimeAndCategory(
                    time[i * 2],
                    time[i * 2 + 1],
                    RecordTransformer.categoryExpenseList
                )
            )
        }
        list
    }

    /**
     * 统计多个时间段内的收入amount和
     * @param time 时间参数,为偶数个,每(n/2,n/2+1)为一对
     * @return 统计结果的list,Float类型
     */
    suspend fun getIncomeAmountStatisticsByTime(time: List<Long>) = withContext(Dispatchers.IO) {
        val list = mutableListOf<Float>()
        for (i in 0 until time.size / 2) {
            list.add(
                recordDao.getAmountStatisticsByTimeAndCategory(
                    time[i * 2],
                    time[i * 2 + 1],
                    RecordTransformer.categoryIncomeList
                )
            )
        }
        list
    }

    /**
     * 统计[startTime,endTime)内的不同类别amount和
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param categories 类别列表
     * @return 统计结果的list,Float类型
     */
    suspend fun getAmountStatisticsByTimeAndCategory(
        startTime: Long,
        endTime: Long,
        categories: List<String>
    ) = withContext(Dispatchers.IO) {
        val list = mutableListOf<Float>()
        categories.forEach { category ->
            list.add(
                recordDao.getAmountStatisticsByTimeAndCategory(
                    startTime,
                    endTime,
                    listOf(category)
                )
            )
        }
        list
    }

    /**
     * 获取数据库中时间最早的记录的时间
     * @return 时间
     */
    suspend fun getEarliestTime() = withContext(Dispatchers.IO) {
        recordDao.getEarliestTime()
    }
}