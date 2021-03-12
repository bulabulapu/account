package com.tang.account.ui.detail

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tang.account.R

/*DetailActivity的方式选择弹窗*/
class WayDialogFragment(viewModel: ViewModel) : DialogFragment() {

    private val adapter = WayRecyclerAdapter(viewModel, this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.dialog_choose, container, false) // 导入布局
        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        val layoutManager = LinearLayoutManager(context)
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter
        view.findViewById<TextView>(R.id.cancel_button).setOnClickListener { // 取消操作
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
}