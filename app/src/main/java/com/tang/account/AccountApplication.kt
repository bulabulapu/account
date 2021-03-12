package com.tang.account

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.widget.Toast

/*全局通用变量类*/
class AccountApplication : Application() {
    companion object {
        const val REQUEST_GET_EXCEL = 0 //ImportFromExcelActivity选择文件的Activity标志
        const val REQUEST_IMPORT_EXCEL = 1 //ImportFromExcelActivity标志
        const val REQUEST_GET_MI_BAK = 2 //ImportFromMIActivity选择文件的Activity标志
        const val REQUEST_IMPORT_MI_BAK = 3 //ImportFromMIActivity标志
        const val REQUEST_DETAIL = 4 //启动DetailActivity查看标志
        const val REQUEST_ADD_RECORD = 5 //启动DetailActivity添加标志
        const val REQUEST_CATEGORY_DETAIL = 6 //CategoryDetailActivity标志
        const val REQUEST_STATISTICS = 7 // StatisticsActivity标志
        const val REQUEST_DETAIL_PREFERENCE = 8 // DetailPreferenceActivity标志

        lateinit var context: Context // 全局Context
        private lateinit var toast: Toast

        /**
         * 全局的toast
         *
         * @param  string toast显示的内容
         * */
        fun showToast(string: String) {
            toast.setText(string)
            toast.show()
        }
    }

    @SuppressLint("ShowToast")
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        toast = Toast.makeText(context, null, Toast.LENGTH_SHORT)
    }
}