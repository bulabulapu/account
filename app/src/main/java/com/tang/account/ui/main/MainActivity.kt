package com.tang.account.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.tang.account.AccountApplication
import com.tang.account.MyAppCompatActivity
import com.tang.account.R
import com.tang.account.databinding.ActivityMainBinding
import com.tang.account.logic.util.OtherUtil.delayExecute
import com.tang.account.model.Record
import com.tang.account.logic.util.RecordTransformer
import com.tang.account.logic.util.TimeUtil
import com.tang.account.viewmodel.MainPageViewModel
import com.tang.account.ui.RecordRecyclerAdapter
import com.tang.account.ui.mybasewidget.TipDialogFragment
import com.tang.account.ui.detail.DetailActivity
import com.tang.account.ui.exporttoexcel.ExportToExcelActivity
import com.tang.account.ui.importfromexcel.ImportFromExcelActivity
import com.tang.account.ui.importfrommibak.ImportFromMiBakActivity
import com.tang.account.ui.mybasewidget.MyFloatingActionButton
import com.tang.account.ui.statistics.StatisticsActivity

/*主界面activity*/
class MainActivity : MyAppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(MainPageViewModel::class.java)
    }

    /*界面布局*/
    private lateinit var binding: ActivityMainBinding

    /*菜单项list*/
    private val fabMenuList = mutableListOf<MyFloatingActionButton>()
    private lateinit var adapter: RecordRecyclerAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFabMenu()
        val layoutManager = LinearLayoutManager(this)
        binding.recordRecycler.layoutManager = layoutManager
        adapter = RecordRecyclerAdapter(this, viewModel)
        binding.recordRecycler.adapter = adapter // 初始化recycler
        binding.checkboxToolBar.cancelButton.setOnClickListener { // 取消按钮事件
            viewModel.clearChecked()
            viewModel.isCheckedAll.value = false
            viewModel.isCheckedStatus.value = false
        }
        binding.checkboxToolBar.selectAllButton.setOnClickListener { // 全选按钮事件
            if (viewModel.isCheckedAll.value == true) {
                binding.checkboxToolBar.selectAllButton.setText(R.string.select_all)
                viewModel.isCheckedAll.value = false
            } else {
                binding.checkboxToolBar.selectAllButton.setText(R.string.select_none)
                viewModel.isCheckedAll.value = true
            }
        }
        binding.statisticsButton.setOnClickListener { // 统计按钮事件
            startActivityForResult(
                Intent(this, StatisticsActivity::class.java),
                AccountApplication.RESULT_STATISTICS
            )
        }
        viewModel.amountStatistics.observe(this, {  // 更改统计数据并显示
            viewModel.setHeaderParam(viewModel.todayAmount, viewModel.monthAmount)
            adapter.notifyItemChanged(0, 0)
        })
        viewModel.loadListFromDB.observe(this, { flag -> // 加载数据完成
            if (flag) {
                adapter.notifyItemRangeInserted( // 更改显示
                    viewModel.displayStartPosition,
                    viewModel.getListSize() - viewModel.displayStartPosition
                )
                viewModel.refreshAmountStatistics()
            }
        })
        viewModel.isCheckedStatus.observe(this, { isChecked -> // 多选状态监听,根据状态改变界面
            if (isChecked) { // 进入多选状态
                binding.addRecordFab.hide()
                binding.mainPageToolBar.visibility = View.GONE
                binding.checkboxToolBar.root.visibility = View.VISIBLE
                delayExecute(this, 200) {
                    binding.deleteRecordFab.show()
                }
            } else {
                viewModel.clearChecked()
                binding.mainPageToolBar.visibility = View.VISIBLE
                binding.checkboxToolBar.root.visibility = View.GONE
                binding.deleteRecordFab.hide()
                delayExecute(this, 200) {
                    binding.addRecordFab.show()
                }
            }
        })
        viewModel.isCheckedAll.observe(this, { flag -> // 全选状态
            viewModel.clearChecked()
            if (flag) {
                for (r in viewModel.getList()) {
                    if (r.id > 0) {
                        viewModel.addChecked(r.id)
                    }
                }
            } else {
                binding.checkboxToolBar.selectAllButton.setText(R.string.select_all)
            }
        })
        viewModel.onCheckedChange.observe(this, { // 监听每次选择操作
            binding.checkboxToolBar.selectedCountText.text =
                "已选中" + viewModel.getCheckedCount() + "项"
        })
        viewModel.deleteFinished.observe(this, { flag -> // 删除完成后的操作
            if (flag) {
                viewModel.isCheckedAll.value = false
                viewModel.isCheckedStatus.value = false
                viewModel.clearList()
                adapter.notifyDataSetChanged()
                viewModel.loadAllRecords()
                binding.progressBar.visibility = View.GONE
                binding.recordRecycler.visibility = View.VISIBLE
            }
        })
        viewModel.insertFinished.observe(this, { position ->
            if (position.isNotEmpty()) {
                val s = position.split(" ")
                adapter.notifyItemRangeInserted(s[0].toInt(), s[1].toInt())
            }
        })
        binding.titleText.setText(R.string.my_expense)
        viewModel.loadAllRecords()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) { // 其他活动结束后的处理
            AccountApplication.RESULT_IMPORT_EXCEL -> if (resultCode == Activity.RESULT_OK) { // activity:导入excel
                val status = data?.getBooleanExtra("status", false)
                if (status == true) {
                    viewModel.clearList()
                    adapter.notifyDataSetChanged()
                    viewModel.loadAllRecords()
                }
            }
            AccountApplication.RESULT_IMPORT_MI_BAK -> if (resultCode == Activity.RESULT_OK) { // activity:导入MIUI备份文件
                val status = data?.getBooleanExtra("status", false)
                if (status == true) {
                    viewModel.clearList()
                    adapter.notifyDataSetChanged()
                    viewModel.loadAllRecords()
                }
            }
            AccountApplication.RESULT_DETAIL -> if (resultCode == Activity.RESULT_OK) { // activity:查看record详细
                when (data?.getIntExtra("operation", -1)) {
                    DetailActivity.OPERATION_SAVE -> { // 保存操作
                        (data.getSerializableExtra("data") as Record).let { // 将更改后的record进行替换
                            if (it.getTime() == viewModel.getTimeByPosition(viewModel.clickedPosition) &&
                                judgeNeedToDisplay(it)
                            ) { // 时间一致且需要显示时
                                viewModel.alterRecord(it)
                                adapter.notifyItemChanged(viewModel.clickedPosition)
                            } else { //时间不一致
                                removeClickedRecord()
                                if (judgeNeedToDisplay(it)) { // 需要显示
                                    viewModel.insertRecord(it)
                                }
                            }
                            viewModel.refreshAmountStatistics()
                        }
                    }
                    DetailActivity.OPERATION_DELETE -> { // 删除操作
                        removeClickedRecord()
                        viewModel.refreshAmountStatistics()
                    }
                }
            }
            AccountApplication.RESULT_ADD_RECORD -> if (resultCode == Activity.RESULT_OK) { // activity:添加record
                val operation = data?.getIntExtra("operation", -1)
                (data?.getSerializableExtra("data") as Record).let {
                    if (operation == DetailActivity.OPERATION_SAVE && judgeNeedToDisplay(it)) { //进行保存操作且满足以下条件时添加显示(添加为支出类且当前显示不为我的收入 或者 添加为收入类且当前显示不为我的支出)
                        viewModel.insertRecord(it)
                        viewModel.refreshAmountStatistics()
                    }
                }
            }
            AccountApplication.RESULT_STATISTICS -> if (resultCode == Activity.RESULT_OK) { // activity:统计activity
                val operation = data?.getIntExtra("operation", -1)
                if (operation == StatisticsActivity.OPERATION_DATA_FIXED) {
                    viewModel.clearList()
                    adapter.notifyDataSetChanged()
                    viewModel.loadAllRecords()
                }
            }
        }
    }

    /**
     * 判断r是否需要在recyclerView中显示
     * @param r 新增的或者修改的记录
     * @return 判断结果
     */
    private fun judgeNeedToDisplay(r: Record) =
        (RecordTransformer.categoryExpenseList.contains(r.getCategory()) && viewModel.currentDisplayStatus != viewModel.STATUS_MY_INCOME)
                || (!RecordTransformer.categoryExpenseList.contains(r.getCategory()) && viewModel.currentDisplayStatus != viewModel.STATUS_MY_EXPENSE)

    override fun onKeyUp(keyCode: Int, event: KeyEvent?) =
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 更改返回按钮操作
            if (binding.menuArea.visibility == View.VISIBLE) { // 打开菜单状态
                menuHide()
                false
            } else if (viewModel.isCheckedStatus.value == true) { // 多选状态
                viewModel.clearChecked()
                viewModel.isCheckedAll.value = false
                viewModel.isCheckedStatus.value = false
                false
            } else {
                super.onKeyUp(keyCode, event)
            }
        } else {
            super.onKeyUp(keyCode, event)
        }

    /**
     * 菜单初始化
     */
    private fun initFabMenu() {
        binding.incomeFab.setLabel(R.string.income)
        binding.expenseAndIncomeFab.setLabel(R.string.expense_and_income)
        binding.exportToExcelFab.setLabel(R.string.export_to_excel)
        binding.importFromExcelFab.setLabel(R.string.import_from_excel)
        binding.importFromMiFab.setLabel(R.string.import_from_mi) // 菜单项设置标签
        binding.incomeFab.setIcon(R.drawable.icon_income)
        binding.expenseAndIncomeFab.setIcon(R.drawable.icon_expense_and_income)
        binding.exportToExcelFab.setIcon(R.drawable.icon_export)
        binding.importFromExcelFab.setIcon(R.drawable.icon_import)
        binding.importFromMiFab.setIcon(R.drawable.icon_import) // 菜单项设置图标
        binding.incomeFab.setOnclickListener { // 收入按钮事件监听
            if (viewModel.currentDisplayStatus == viewModel.STATUS_MY_INCOME) { // 显示界面为收入界面时
                viewModel.currentDisplayStatus = viewModel.STATUS_MY_EXPENSE
                viewModel.clearList()
                adapter.notifyDataSetChanged()
                viewModel.loadAllRecords()
                binding.incomeFab.setLabel(R.string.income)
                binding.incomeFab.setIcon(R.drawable.icon_income)
                binding.titleText.setText(R.string.my_expense)
            } else { // 其他界面
                viewModel.currentDisplayStatus = viewModel.STATUS_MY_INCOME
                viewModel.clearList()
                adapter.notifyDataSetChanged()
                viewModel.loadAllRecords()
                binding.incomeFab.setLabel(R.string.expense)
                binding.incomeFab.setIcon(R.drawable.icon_expense)
                binding.titleText.setText(R.string.my_income)
            }
            binding.expenseAndIncomeFab.setLabel(R.string.expense_and_income)
            binding.expenseAndIncomeFab.setIcon(R.drawable.icon_expense_and_income)
            menuHide()
        }
        binding.expenseAndIncomeFab.setOnclickListener { // 收支按钮监听
            if (viewModel.currentDisplayStatus == viewModel.STATUS_MY_EXPENSE_AND_INCOME) { // 显示界面为收支界面时
                viewModel.currentDisplayStatus = viewModel.STATUS_MY_EXPENSE
                viewModel.clearList()
                adapter.notifyDataSetChanged()
                viewModel.loadAllRecords()
                binding.expenseAndIncomeFab.setLabel(R.string.expense_and_income)
                binding.expenseAndIncomeFab.setIcon(R.drawable.icon_expense_and_income)
                binding.titleText.setText(R.string.my_expense)
            } else { // 其他界面
                viewModel.currentDisplayStatus = viewModel.STATUS_MY_EXPENSE_AND_INCOME
                viewModel.clearList()
                adapter.notifyDataSetChanged()
                viewModel.loadAllRecords()
                binding.expenseAndIncomeFab.setLabel(R.string.expense)
                binding.expenseAndIncomeFab.setIcon(R.drawable.icon_expense)
                binding.titleText.setText(R.string.my_expense_and_income)
            }
            binding.incomeFab.setLabel(R.string.income)
            binding.incomeFab.setIcon(R.drawable.icon_income)
            menuHide()
        }
        binding.exportToExcelFab.setOnclickListener { // 导出excel按钮监听
            startActivity(Intent(this, ExportToExcelActivity::class.java))
            delayExecute(this, 200) {
                menuHide()
            }
        }
        binding.importFromExcelFab.setOnclickListener { // 导入excel按钮事件
            startActivityForResult(
                Intent(this, ImportFromExcelActivity::class.java),
                AccountApplication.RESULT_IMPORT_EXCEL
            )
            delayExecute(this, 200) {
                menuHide()
            }
        }
        binding.importFromMiFab.setOnclickListener { //导入MIBak按钮事件
            startActivityForResult(
                Intent(this, ImportFromMiBakActivity::class.java),
                AccountApplication.RESULT_IMPORT_MI_BAK
            )
            delayExecute(this, 200) {
                menuHide()
            }
        }
        fabMenuList.add(binding.expenseAndIncomeFab)
        fabMenuList.add(binding.incomeFab)
        fabMenuList.add(binding.exportToExcelFab)
        fabMenuList.add(binding.importFromExcelFab)
        fabMenuList.add(binding.importFromMiFab) // fabMenuList初始化
        binding.addRecordFab.setOnLongClickListener { // 添加按钮长按事件监听,打开菜单
            if (binding.menuArea.visibility != View.VISIBLE) {
                menuShow()
            }
            return@setOnLongClickListener true
        }
        binding.addRecordFab.setOnClickListener { // 添加按钮单击事件
            if (binding.menuArea.visibility == View.VISIBLE) {
                menuHide()
            } else {
                DetailActivity.actionStart(
                    this, Record(
                        RecordTransformer.CAN_YIN,
                        Record.DEFAULT_ILLEGAL_AMOUNT,
                        TimeUtil.getNowTime(),
                        RecordTransformer.ZHI_FU_BAO
                    ),
                    AccountApplication.RESULT_ADD_RECORD
                )
            }
        }
        binding.deleteRecordFab.setOnClickListener {
            TipDialogFragment.show(this, "确认删除已选中记录?") {
                binding.recordRecycler.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                viewModel.deleteSelectedRecords()
            }
        }
        binding.menuArea.setOnClickListener {
            menuHide()
        }
    }

    /**
     * 显示菜单
     */
    private fun menuShow() {
        binding.menuArea.visibility = View.VISIBLE
        for (fab in fabMenuList) { // 依次显示菜单项
            delayExecute(this, 50L * fabMenuList.indexOf(fab)) {
                fab.show()
            }
        }
    }

    /**
     * 隐藏菜单
     */
    private fun menuHide() {
        for (fab in fabMenuList) { // 隐藏每个菜单项
            fab.hide()
        }
        delayExecute(this, 200) { // 动画完成后隐藏整个区域
            binding.menuArea.visibility = View.INVISIBLE
        }
    }

    /**
     *移除操作位置的record(点击进入详情界面的record)
     */
    private fun removeClickedRecord() {
        viewModel.removeClickedRecord()
        adapter.notifyItemRemoved(viewModel.clickedPosition)
        adapter.notifyItemRangeChanged(
            viewModel.clickedPosition,
            viewModel.getListSize() - viewModel.clickedPosition
        )
        viewModel.clickedPosition--
        if (viewModel.getIdByPosition(viewModel.clickedPosition) == RecordRecyclerAdapter.ID_DATE && (
                    viewModel.clickedPosition + 1 == viewModel.getListSize() ||
                            viewModel.getIdByPosition(viewModel.clickedPosition + 1) == RecordRecyclerAdapter.ID_DATE)
        ) { // 移除的记录的前一个item为日期item,且这个日期item为最后一个item或者下一个item也为日期item时,移除当前item
            removeClickedRecord()
        }
    }
}
