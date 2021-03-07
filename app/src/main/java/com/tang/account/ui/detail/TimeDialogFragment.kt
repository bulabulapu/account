package com.tang.account.ui.detail

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.tang.account.R
import com.tang.account.logic.util.TimeUtil
import com.tang.account.viewmodel.DetailViewModel

/*DetailActivity的时间选择弹窗*/
class TimeDialogFragment(private val viewModel: DetailViewModel) : DialogFragment(),
    View.OnFocusChangeListener {

    /*年输入框*/
    private lateinit var yearText: EditText

    /*月输入框*/
    private lateinit var monthText: EditText

    /*日输入框*/
    private lateinit var dayText: EditText

    /*时输入框*/
    private lateinit var hourText: EditText

    /*分输入框*/
    lateinit var minuteText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.dialog_time, container, false) // 导入布局
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        yearText = view.findViewById(R.id.year_text)
        monthText = view.findViewById(R.id.month_text)
        dayText = view.findViewById(R.id.day_text)
        hourText = view.findViewById(R.id.hour_text)
        minuteText = view.findViewById(R.id.minute_text)
        yearText.onFocusChangeListener = this
        monthText.onFocusChangeListener = this
        dayText.onFocusChangeListener = this
        hourText.onFocusChangeListener = this
        minuteText.onFocusChangeListener = this // 为所有输入框添加文字更改监听
        view.findViewById<View>(R.id.outside).setOnClickListener { // 取消操作
            dismiss()
        }
        view.findViewById<TextView>(R.id.cancel_button).setOnClickListener { // 取消操作
            dismiss()
        }
        view.findViewById<TextView>(R.id.sure_button).setOnClickListener {// 确认操作,判断输入合法性
            val year = yearText.text.toString().toInt()
            val month = monthText.text.toString().toInt()
            val day = dayText.text.toString().toInt()
            val hour = hourText.text.toString().toInt()
            val minute = minuteText.text.toString().toInt()
            val checkList = TimeUtil.check(year, month, day, hour, minute)
            var flag = true
            for (i in checkList.indices) {
                if (!checkList[i]) {
                    setErrorBord(i)
                    flag = false
                }
            }
            if (flag) { // 合法,更改时间
                viewModel.setTime(TimeUtil.getTimeStamp(year, month, day, hour, minute))
                dismiss()
            }
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        val windows = dialog?.window
        val params = windows?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        windows?.attributes = params // 控件位置
        windows?.setWindowAnimations(R.style.dialogAnimStyle) // 显示和隐藏动画
        windows?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 背景透明
        val time = viewModel.time.value as Long
        yearText.setText(TimeUtil.stampToYear(time).toString())
        monthText.setText(TimeUtil.stampToMonth(time).toString())
        dayText.setText(TimeUtil.stampToDate(time).toString())
        hourText.setText(TimeUtil.stampToTime(time).split(":")[0])
        minuteText.setText(TimeUtil.stampToTime(time).split(":")[1]) // 读取此record时间并显示
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) { // 点击时更改边框
        v?.let {
            if (hasFocus) {
                it.setBackgroundResource(R.drawable.focus_card_border)
            } else {
                it.setBackgroundResource(R.drawable.not_focus_card_border)
            }
        }
    }

    /**
     * 设置边框为错误类型
     *
     * @param i 错误边框的位次
     */
    private fun setErrorBord(i: Int) {
        when (i) {
            0 -> yearText.setBackgroundResource(R.drawable.error_card_border)
            1 -> monthText.setBackgroundResource(R.drawable.error_card_border)
            2 -> dayText.setBackgroundResource(R.drawable.error_card_border)
            3 -> hourText.setBackgroundResource(R.drawable.error_card_border)
            4 -> minuteText.setBackgroundResource(R.drawable.error_card_border)
        }
    }
}