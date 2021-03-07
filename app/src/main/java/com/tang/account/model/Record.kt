package com.tang.account.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/*数据实体类*/
@Entity
data class Record(
    /*类型*/
    private var category: String,
    /*金额*/
    private var amount: Float,
    /*时间戳*/
    private var time: Long,
    /*方式*/
    private var way: String,
    /*详情*/
    private var info: String = ""
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = DEFAULT_ID

    companion object {
        /*默认id值*/
        const val DEFAULT_ID = 0L

        /*默认非法金额*/
        const val DEFAULT_ILLEGAL_AMOUNT = -1f
    }

    /**
     * 金额降序排序器
     */
    class AmountDesComparator : Comparator<Record> {
        override fun compare(o1: Record?, o2: Record?): Int {
            if (o1 != null && o2 != null) {
                val sub = o1.amount - o2.amount
                return when {
                    sub > 0f -> -1
                    sub == 0f -> 0
                    else -> 1
                }
            }
            return 0
        }
    }

    /**
     * 时间降序排序器
     */
    class TimeDesComparator : Comparator<Record> {
        override fun compare(o1: Record?, o2: Record?): Int {
            if (o1 != null && o2 != null) {
                val sub = o1.time - o2.time
                return when {
                    sub > 0L -> -1
                    sub == 0L -> 0
                    else -> 1
                }
            }
            return 0
        }
    }

    /**
     * @return 类型
     */
    fun getCategory(): String {
        return category
    }

    /**
     * @return 金额
     */
    fun getAmount(): Float {
        return formatAmount(amount)
    }

    /**
     * @return 时间戳
     */
    fun getTime(): Long {
        return time
    }

    /**
     * @return 方式
     */
    fun getWay(): String {
        return way
    }

    /**
     * @return 详情
     */
    fun getInfo(): String {
        return info
    }

    /**
     *设置类型
     * @param category 类型
     */
    fun setCategory(category: String) {
        this.category = category
    }

    /**
     * 设置金额
     * @param amount 金额
     */
    fun setAmount(amount: Float) {
        this.amount = formatAmount(amount)
    }

    /**
     * 设置时间戳
     * @param time 时间戳
     */
    fun setTime(time: Long) {
        this.time = time
    }

    /**
     * 设置方式
     * @param way 方式
     */
    fun setWay(way: String) {
        this.way = way
    }

    /**
     * 设置详细
     * @param info 详细内容
     */
    fun setInfo(info: String) {
        this.info = info
    }

    /**
     * 金额格式化(四舍五入保留小数点后两位)
     * @param input 输入的金额
     * @return 格式化后的金额
     */
    private fun formatAmount(input: Float): Float {
        var a: Int = (input * 1000).toInt()
        if (a % 10 >= 5) {
            a += 10
        }
        a /= 10
        return a.toFloat() / 100
    }

}
