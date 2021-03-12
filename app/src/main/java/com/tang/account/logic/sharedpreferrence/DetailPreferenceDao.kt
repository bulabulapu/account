package com.tang.account.logic.sharedpreferrence

import android.content.Context
import com.tang.account.AccountApplication
import com.tang.account.logic.util.RecordTransformer

/*详细界面偏好设置Dao*/
object DetailPreferenceDao {
    private val sharedPrefer =
        AccountApplication.context.getSharedPreferences("detail_preference", Context.MODE_PRIVATE)

    /**
     * 保存偏好设置
     * @param categoryPrefer 类别偏好
     * @param wayPrefer 支付方式偏好
     */
    fun save(categoryPrefer: String, wayPrefer: String) {
        sharedPrefer.edit().apply {
            putString("category", categoryPrefer)
            putString("way", wayPrefer)
            apply()
        }
    }

    /**
     * 读取偏好设置
     * @return 以map方式返回偏好设置
     */
    fun load(): Map<String, String> {
        return mapOf(
            "category_preference" to sharedPrefer.getString(
                "category",
                RecordTransformer.CAN_YIN
            )!!,
            "way_preference" to sharedPrefer.getString("way", RecordTransformer.ZHI_FU_BAO)!!
        )
    }
}