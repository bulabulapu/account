package com.tang.account.ui.statistics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.tang.account.AccountApplication
import com.tang.account.R
import com.tang.account.logic.util.RecordTransformer
import com.tang.account.ui.statistics.categorydetail.CategoryDetailActivity
import com.tang.account.viewmodel.StatisticsViewModel
import de.hdodenhof.circleimageview.CircleImageView

/*StatisticsActivity的recycler适配器*/
class StatisticsRecyclerAdapter(
    private val activity: AppCompatActivity,
    private val viewModel: StatisticsViewModel
) :
    RecyclerView.Adapter<StatisticsRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /*类别图标*/
        val categoryImage: CircleImageView = view.findViewById(R.id.category_image)

        /*类别文字*/
        val categoryText: TextView = view.findViewById(R.id.category_text)

        /*金额*/
        val amountText: TextView = view.findViewById(R.id.amount_text)

        /*表示占比的横向有色条*/
        val line: View = view.findViewById(R.id.line)

        /*整个item*/
        val body = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.statistics_item, parent, false)
    )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false) // 禁用复用
        val r = viewModel.statisticsListPart[position]
        holder.categoryImage.setImageResource(RecordTransformer.getImageId(r.getCategory())) // 设置图标
        holder.categoryText.text = RecordTransformer.transform(r.getCategory()) // 设置类别文字
        holder.amountText.text = r.getAmount().toString() // 设置金额
        holder.line.setBackgroundResource(RecordTransformer.getCategoryColor(r.getCategory())) // 设置有色条颜色
        val layoutParams = holder.line.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = r.getInfo().toFloat() // 设置有色条长度
        holder.body.setOnClickListener { // 点击事件(查看此时间段内当前类别的详细record)
            val record = viewModel.statisticsListTotal[viewModel.selectedBarPosition.value!!]
            val s = record.getInfo().split(" ")
            CategoryDetailActivity.actionStart(
                activity,
                r.getCategory(),
                s[2].toLong(),
                s[3].toLong(),
                s[0],
                AccountApplication.RESULT_CATEGORY_DETAIL
            )
        }
    }

    override fun getItemCount(): Int = viewModel.statisticsListPart.size

}