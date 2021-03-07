package com.tang.account.logic.database

import androidx.room.*
import com.tang.account.model.Record

/*本地数据库Dao接口*/
@Dao
interface RecordDao {

    /**
     * 新增一条记录
     * @param record 新增的记录
     * @return 新增记录的id值
     */
    @Insert
    fun insertRecord(record: Record): Long

    /**
     * 更改一条记录
     * @param newRecord 更新后的record
     */
    @Update
    fun updateRecord(newRecord: Record)

    /**
     * 清空类别在categories内的记录
     * @param categories 类别
     */
    @Query("delete from Record where category in (:categories)")
    fun deleteRecords(categories: List<String>)

    /**
     * 加载endT前count条记录,不包括endTime,并按照time值ime降序排列
     * @param endTime 结束时间
     * @param count 数量
     * @return record列表
     */
    @Query("select * from Record where  time < :endTime order by time DESC limit :count")
    fun loadRecordsByTimeAndCount(endTime: Long, count: Int): List<Record>

    /**
     * 加载[startTime,endTime)内的类别在categories内的记录,并按照time值降序排列
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param categories 类别
     * @return record列表
     */
    @Query("select * from Record where  time >= :startTime and time < :endTime and category in (:categories) order by time DESC")
    fun loadRecordsByTimeAndCategory(
        startTime: Long,
        endTime: Long,
        categories: List<String>
    ): List<Record>

    /**
     * 通过id值删除一条记录
     * @param id 被删除记录的id值
     */
    @Query("delete from Record where id = :id")
    fun deleteRecordById(id: Long)

    /**
     * 统计[startTime,endTime)内类别在categories内的记录的amount和
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param categories 类别
     * @return 统计结果
     */
    @Query("select sum(amount) from Record where time >= :startTime and time < :endTime and category in (:categories)")
    fun getAmountStatisticsByTimeAndCategory(
        startTime: Long,
        endTime: Long,
        categories: List<String>
    ): Float

    /**
     * 获取数据库中时间最早的记录的时间
     * @return 时间
     */
    @Query("select min(time) from Record")
    fun getEarliestTime(): Long
}