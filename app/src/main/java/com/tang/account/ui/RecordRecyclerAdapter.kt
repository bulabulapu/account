package com.tang.account.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tang.account.AccountApplication
import com.tang.account.R
import com.tang.account.logic.util.OtherUtil.delayExecute
import com.tang.account.logic.util.RecordTransformer
import com.tang.account.logic.util.TimeUtil
import com.tang.account.model.Record
import com.tang.account.viewmodel.CheckedRecordViewModel
import com.tang.account.viewmodel.MainPageViewModel
import com.tang.account.ui.detail.DetailActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.IllegalArgumentException
import kotlin.concurrent.thread

/*recyclerview适配器类*/
class RecordRecyclerAdapter(
    private val activity: AppCompatActivity,
    private val viewModel: CheckedRecordViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /*记录类型*/
    private val RECORD_TYPE = 0

    /*日期类型*/
    private val DATE_TYPE = 1

    /*头部类型*/
    private val HEADER_TYPE = 2

    /*多选框大小*/
    private var checkboxImageSize = 0

    /*多选框显示动画*/
    private val scaleUp =
        AnimationUtils.loadAnimation(activity.applicationContext, R.anim.scale_up_image)

    /*多选框隐藏动画*/
    private val scaleDown =
        AnimationUtils.loadAnimation(activity.applicationContext, R.anim.scale_down_image)

    /*多选框动画持续时间*/
    private val animationKeep = 100L

    init { // 初始化确认选择框大小
        checkboxImageSize =
            (activity.applicationContext.resources.displayMetrics.density * 25f + 0.5f).toInt()
    }

    companion object {
        const val ID_HEAD = -2L
        const val ID_DATE = -1L
    }

    /*记录类型ViewHolder*/
    class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /*类别图标*/
        val categoryImage: CircleImageView = view.findViewById(R.id.category_image)

        /*详情*/
        val infoText: TextView = view.findViewById(R.id.info_text)

        /*金额*/
        val amountText: TextView = view.findViewById(R.id.amount_text)

        /*方式*/
        val wayText: TextView = view.findViewById(R.id.way_text)

        /*时间*/
        val timeText: TextView = view.findViewById(R.id.time_text)

        /*选择框*/
        val checkboxImage: CircleImageView = view.findViewById(R.id.checkbox_image)
    }

    /*日期类型ViewHolder*/
    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /*日期*/
        val dateText: TextView = view.findViewById(R.id.date_text)
    }

    /*头部类型ViewHolder*/
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /*今日累计*/
        val todayStatistics: TextView = view.findViewById(R.id.today_statistics)

        /*本月累计*/
        val monthStatistics: TextView = view.findViewById(R.id.month_statistics)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) { // 根据不同ViewHolder导入不同布局
            RECORD_TYPE -> RecordViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.record_item, parent, false)
            )
            DATE_TYPE -> DateViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.date_item, parent, false)
            )
            else -> HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.header_item, parent, false)
            )
        }

    /**
     * recycler大小
     */
    override fun getItemCount() = viewModel.getListSize()

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val record = viewModel.getRecordAt(position) as Record // 当前记录
        val recordId = record.id
        when (holder) {
            is RecordViewHolder -> { // 为记录类型
                thread {
                    val image = RecordTransformer.getImageId(record.getCategory()) // 类别图标
                    val info = record.getInfo()
                    var category = RecordTransformer.transform(record.getCategory())
                    if (info.isNotEmpty()) {
                        category = "$category-$info"
                    } // 类别和详细信息
                    var amount = record.getAmount().toString() + "元" // 金额
                    if (viewModel is MainPageViewModel && viewModel.currentDisplayStatus == viewModel.STATUS_MY_EXPENSE_AND_INCOME) { // 主界面我的收支
                        amount =
                            if (RecordTransformer.categoryExpenseList.contains(record.getCategory())) { // 为支出记录
                                "-$amount"
                            } else {
                                "+$amount"
                            }
                    }
                    val way = RecordTransformer.transform(record.getWay()) // 方式
                    var time: String
                    if (viewModel is MainPageViewModel) { // 为主界面,格式为 周X xx:xx
                        time = TimeUtil.stampToTime(record.getTime())
                        while (time.length < 5) {
                            time = "  $time" // 位置对齐
                        }
                        time = TimeUtil.stampToDay(record.getTime()) + " $time"

                    } else { // 其他界面,格式为 xxxx/xx/xx xx:xx
                        time =
                            TimeUtil.stampToYear(record.getTime())
                                .toString() + "/" + TimeUtil.stampToMonth(record.getTime()) + "/" + TimeUtil.stampToDate(
                                record.getTime()
                            ) + " " + TimeUtil.stampToTime(record.getTime())
                    } // 时间
                    activity.runOnUiThread { // 显示
                        holder.categoryImage.setImageResource(image)
                        holder.infoText.text = category
                        holder.amountText.text = amount
                        holder.wayText.text = way
                        holder.timeText.text = time
                    }
                }
                if (!RecordTransformer.categoryExpenseList.contains(record.getCategory())) { // 当前记录为收入记录
                    holder.amountText.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.incomeAmountTextColor
                        )
                    )
                } else {
                    holder.amountText.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.textColorPrimary
                        )
                    )
                }
                /*记录类型监测全选状态*/
                viewModel.isCheckedAll.observe(activity, { flag ->
                    if (flag) { // 全选显示已选择图标
                        holder.checkboxImage.setImageResource(R.drawable.checkbox_on)
                    } else { // 全不选显示未选择图标
                        holder.checkboxImage.setImageResource(R.drawable.checkbox_down)
                    }
                })
                /*记录类型监测多选状态*/
                viewModel.isCheckedStatus.observe(
                    activity, { isChecked ->
                        if (isChecked) { // 进入多选状态时更改点击监听,更改view显示
                            if (viewModel.hasChecked(recordId)) { // 监测当前record是否被选择
                                holder.checkboxImage.setImageResource(R.drawable.checkbox_on)
                            } else {
                                holder.checkboxImage.setImageResource(R.drawable.checkbox_down)
                            }
                            val params = holder.checkboxImage.layoutParams
                            params.height = checkboxImageSize
                            params.width = checkboxImageSize
                            holder.checkboxImage.layoutParams = params // 设置选择框大小参数
                            holder.itemView.setOnClickListener { // 单击事件监听
                                holder.checkboxImage.startAnimation(scaleDown) // 执行隐藏动画
                                if (viewModel.hasChecked(recordId)) { // 根据当前record选择状态进行不同操作
                                    viewModel.removeChecked(recordId)
                                    delayExecute(activity, animationKeep) {
                                        holder.checkboxImage.setImageResource(R.drawable.checkbox_down)
                                        holder.checkboxImage.startAnimation(scaleUp)
                                    }
                                } else {
                                    viewModel.addChecked(recordId)
                                    delayExecute(activity, animationKeep) {
                                        holder.checkboxImage.setImageResource(R.drawable.checkbox_on)
                                        holder.checkboxImage.startAnimation(scaleUp)
                                    }
                                }
                            }
                            holder.itemView.setOnLongClickListener {// 长按操作事件监听,与单击事件执行相同操作
                                holder.checkboxImage.startAnimation(scaleDown)
                                if (viewModel.hasChecked(recordId)) {
                                    viewModel.removeChecked(recordId)
                                    delayExecute(activity, animationKeep) {
                                        holder.checkboxImage.setImageResource(R.drawable.checkbox_down)
                                        holder.checkboxImage.startAnimation(scaleUp)
                                    }
                                } else {
                                    viewModel.addChecked(recordId)
                                    delayExecute(activity, animationKeep) {
                                        holder.checkboxImage.setImageResource(R.drawable.checkbox_on)
                                        holder.checkboxImage.startAnimation(scaleUp)
                                    }
                                }
                                return@setOnLongClickListener true
                            }
                        } else { // 未进入多选状态
                            val params = holder.checkboxImage.layoutParams
                            params.height = 0
                            params.width = 0
                            holder.checkboxImage.layoutParams = params // 设置多选框大小参数(隐藏)
                            holder.itemView.setOnClickListener { // 单击事件
                                if (viewModel is MainPageViewModel) { // 更改操作位置(主界面下)
                                    viewModel.clickedPosition = position
                                }
                                DetailActivity.actionStart( // 查看record
                                    activity,
                                    record,
                                    AccountApplication.RESULT_DETAIL
                                )
                            }
                            holder.itemView.setOnLongClickListener { // 长按事件
                                viewModel.isCheckedStatus.value = true // 进入多选状态
                                viewModel.addChecked(recordId)
                                holder.checkboxImage.setImageResource(R.drawable.checkbox_on)
                                return@setOnLongClickListener true
                            }
                        }
                    })
            }
            is DateViewHolder -> { // 为日期类型
                thread { // 设置显示日期
                    val date = TimeUtil.stampToMonth(record.getTime())
                        .toString() + "月" + TimeUtil.stampToDate(
                        record.getTime()
                    ) + "日"
                    activity.runOnUiThread {
                        holder.dateText.text = date
                    }
                }
            }
            is HeaderViewHolder -> { // 头部类型
                val today = record.getAmount().toString()
                val month = record.getInfo()
                holder.todayStatistics.text = today
                holder.monthStatistics.text = month // 显示统计信息
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int) = when (viewModel.getRecordAt(position)?.id) {
        ID_DATE -> DATE_TYPE // id为-1的为日期类型
        ID_HEAD -> HEADER_TYPE // id为-2的为头部类型
        else -> RECORD_TYPE
    }
}