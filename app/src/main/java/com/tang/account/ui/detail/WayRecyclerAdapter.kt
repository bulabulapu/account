package com.tang.account.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.tang.account.R
import com.tang.account.logic.util.RecordTransformer
import com.tang.account.viewmodel.DetailPreferenceViewModel
import com.tang.account.viewmodel.DetailViewModel

/*DetailActivity的方式选择弹窗中的RecyclerAdapter*/
class WayRecyclerAdapter(
    private val viewModel: ViewModel,
    private val dialogFragment: WayDialogFragment
) :
    RecyclerView.Adapter<WayRecyclerAdapter.ViewHolder>() {

    /*方式列表*/
    private val list = listOf(
        RecordTransformer.ZHI_FU_BAO,
        RecordTransformer.WEI_XIN,
        RecordTransformer.XIAN_JIN,
        RecordTransformer.YIN_HANG_KA,
        RecordTransformer.XIN_YONG_KA,
        RecordTransformer.QI_TA
    )

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.dialog_way_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = RecordTransformer.transform(list[position])
        holder.text.setOnClickListener {
            if (viewModel is DetailViewModel) {
                viewModel.setWay(list[position])
            } else if (viewModel is DetailPreferenceViewModel) {
                viewModel.setWay(list[position])
            }
            dialogFragment.dismiss()
        }
    }
}