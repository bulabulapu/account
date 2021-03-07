package com.tang.account.ui.mybasewidget

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.tang.account.R

/*弹出提示框单例类*/
object TipDialogFragment : DialogFragment() {

    /*提示内容*/
    private lateinit var text: String
    /*确定后的操作*/
    private lateinit var sure: () -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.dialog_tip, container, false) // 导入布局
        view.findViewById<TextView>(R.id.tip_text).text = text // 提示内容
        view.findViewById<TextView>(R.id.cancel_button).setOnClickListener { dismiss() } // 取消操作
        view.findViewById<TextView>(R.id.sure_button).setOnClickListener { // 确认操作
            sure()
            dismiss()
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        val windows = dialog?.window
        val params = windows?.attributes
        params?.gravity = Gravity.BOTTOM
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        windows?.attributes = params // 控件位置
        windows?.setWindowAnimations(R.style.dialogAnimStyle) // 显示和隐藏动画
        windows?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 背景透明
    }

    /**
     * 调用接口
     *
     * @param activity 父活动
     * @param tipText 提示内容
     * @param block 确认后操作
     */
    fun show(
        activity: AppCompatActivity,
        tipText: String,
        block: () -> Unit
    ) {
        text = tipText
        sure = block
        super.show(activity.supportFragmentManager, "tip_dialog")
    }
}