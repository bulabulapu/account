package com.tang.account.logic.util

import java.util.Calendar

/*时间数据工具类*/
object TimeUtil {

    /*一分钟的值*/
    const val ONE_MINUTE = 60000L

    /*一小时的值*/
    const val ONE_HOUR = 3600000L

    /*一天的值*/
    const val ONE_DAY = 86400000L

    /**
     * 返回当前时间戳
     * @return 时间戳
     */
    fun getNowTime(): Long {
        return System.currentTimeMillis()
    }

    /**
     * 时间戳的年份
     * @param stamp 时间戳
     * @return 年
     */
    fun stampToYear(stamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = stamp
        return calendar.get(Calendar.YEAR)
    }

    /**
     * 时间戳的月份
     * @param stamp 时间戳
     * @return 月
     */
    fun stampToMonth(stamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = stamp
        return calendar.get(Calendar.MONTH) + 1
    }

    /**
     * 时间戳的当月的几号
     * @param stamp 时间戳
     * @return 当月的第几天
     */
    fun stampToDate(stamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = stamp
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * 时间戳的当天的时间
     * @param stamp 时间戳
     * @return 当天的几时几分
     */
    fun stampToTime(stamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = stamp
        var link = ":"
        if (calendar.get(Calendar.MINUTE) < 10) {
            link += "0"
        }
        return calendar.get(Calendar.HOUR_OF_DAY).toString() + link + calendar.get(Calendar.MINUTE)
    }

    /**
     * 时间戳的秒数
     * @param stamp 时间戳
     * @return 秒数
     */
    fun stampToSecond(stamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = stamp
        var second = calendar.get(Calendar.SECOND).toString()
        if (second.length == 1) second = "0$second"
        return second
    }

    /**
     * 时间戳的当天为星期几
     * @param stamp 时间戳
     * @return 当天的星期(数字表示)
     */
    fun stampToDayWithNum(stamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = stamp
        val result = calendar.get(Calendar.DAY_OF_WEEK) - 1
        if (result == 0) return 7
        return result
    }

    /**
     * 时间戳的当天为星期几
     * @param stamp 时间戳
     * @return 当天的星期(字符串)
     */
    fun stampToDay(stamp: Long): String {
        when (stampToDayWithNum(stamp)) {
            1 -> return "周一"
            2 -> return "周二"
            3 -> return "周三"
            4 -> return "周四"
            5 -> return "周五"
            6 -> return "周六"
        }
        return "周日"
    }

    /**
     * 时间戳的当周为当年的第几周(第一个周一开始计算)
     * @param stamp 时间戳
     * @return 周数
     */
    fun stampToWeek(stamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = stamp
        var dayNum = calendar.get(Calendar.DAY_OF_YEAR)
        val first = stampToDayWithNum(getTimeStamp(stampToYear(stamp), 1, 1, 1, 1)) // 当年1月1日为星期几
        dayNum += (first - 2)
        return dayNum / 7
    }

    /**
     * 将时间戳转换为excel中的日期
     * @param stamp 时间戳
     * @return 日期
     */
    fun stampToDateForExcel(stamp: Long): String {
        var str: String
        val year = stampToYear(stamp)
        str = year.toString()
        val month = stampToMonth(stamp)
        str += if (month < 10) {
            "0$month"
        } else {
            month
        }
        val day = stampToDate(stamp)
        str += if (day < 10) {
            "0$day"
        } else {
            day
        }
        return str
    }

    /**
     * 将时间戳转换为excel中的时间
     * @param stamp 时间戳
     * @return 时间
     */
    fun stampToTimeForExcel(stamp: Long): String {
        val str = stampToTime(stamp).split(":")
        var result: String
        result = if (str[0].length == 1) {
            "0" + str[0]
        } else {
            str[0]
        }
        if (str[1].length == 1) {
            result = result + "0" + str[1]
        } else {
            result += str[1]
        }
        return result
    }

    /**
     * 返回日期的时间戳
     * @param year 年
     * @param month 月
     * @param day 日
     * @param hour 时
     * @param minute 分
     * @return 时间戳
     */
    fun getTimeStamp(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day, hour, minute)
        return calendar.timeInMillis
    }

    /**
     * 返回日期的时间戳
     * @param str 表示日期的字符串,如202011111000,后四位有时缺一位,如900,表示9时
     * @return 时间戳
     */
    fun getTimeStamp(str: String): Long {
        val year = str.substring(0, 4).toInt()
        val month = str.substring(4, 6).toInt()
        val day = str.substring(6, 8).toInt()
        val hourStr = str.substring(8, str.length - 2)
        var hour = 0
        if (hourStr.isNotEmpty()) {
            hour = hourStr.toInt()
        }
        val minute = str.substring(str.length - 2).toInt()
        return getTimeStamp(year, month, day, hour, minute)
    }

    /**
     * 当天0点时间戳
     * @param stamp 时间戳
     * @return 当天0点的时间戳
     */
    fun getZPZStamp(stamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.set(stampToYear(stamp), stampToMonth(stamp) - 1, stampToDate(stamp), 0, 0, 0)
        val time = calendar.timeInMillis
        return time / 1000 * 1000
    }

    /**
     * 检查日期时间是否合法
     * @param year 年
     * @param month 月
     * @param day 日
     * @param hour 时
     * @param minute 分
     * @return 每一个参数是否合法的一个list
     */
    fun check(year: Int, month: Int, day: Int, hour: Int, minute: Int): List<Boolean> {
        val list = mutableListOf<Boolean>()
        if (year < 1970) {
            list.add(false)
        } else {
            list.add(true)
        }
        if (month > 12 || month < 1) {
            list.add(false)
        } else {
            list.add(true)
        }
        if (getTheNumOfDayInMonth(year, month) < day || day < 1) {
            list.add(false)
        } else {
            list.add(true)
        }
        if (hour < 0 || hour > 23) {
            list.add(false)
        } else {
            list.add(true)
        }
        if (minute < 0 || minute > 59) {
            list.add(false)
        } else {
            list.add(true)
        }
        return list
    }

    /**
     * 检查日期是否合法
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 是否合法
     */
    fun check(year: Int, month: Int, day: Int): Boolean {
        val list = check(year, month, day, 0, 0)
        for (l in list) {
            if (!l) return false
        }
        return true
    }

    /**
     * 某年某月的天数
     * @param year 年
     * @param month 月
     * @return 天数
     */
    fun getTheNumOfDayInMonth(year: Int, month: Int): Int {
        var days = 31
        when (month) {
            2 -> {
                days = if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                    29
                } else {
                    28
                }
            }
            4 -> days = 30
            6 -> days = 30
            9 -> days = 30
            11 -> days = 30
        }
        return days
    }
}