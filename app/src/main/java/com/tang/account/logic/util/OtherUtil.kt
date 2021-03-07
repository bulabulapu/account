package com.tang.account.logic.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import kotlin.concurrent.thread

object OtherUtil {
    /**
     * dip转换为px
     * @param context 上下文环境
     * @param dpValue dip值
     * @return px值
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 自定义延时执行函数
     * @param activity 活动
     * @param millis 推迟时间
     * @param block 执行的操作
     */
    fun delayExecute(activity: AppCompatActivity, millis: Long, block: () -> Unit) {
        thread {
            Thread.sleep(millis)
            activity.runOnUiThread {
                block()
            }
        }
    }


    /**
     * 格式化数值(四舍五入保留两位小数)
     * @param input 需要格式化的数值
     * @return 格式化后的数值
     */
    fun formatFloat2(input: Float): Float {
        var a: Int = (input * 1000).toInt()
        if (a % 10 >= 5) {
            a += 10
        }
        a /= 10
        return a.toFloat() / 100
    }

    /**
     * 更改触发器状态
     * @param trigger 触发器
     */
    fun alterTriggerStatus(trigger: MutableLiveData<Boolean>) {
        trigger.let {
            if (it.value == null) {
                it.value = false
            } else {
                it.value = it.value != true
            }
        }
    }
}