package com.tang.account.ui.exporttoexcel

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.tang.account.AccountApplication
import com.tang.account.MyAppCompatActivity
import com.tang.account.R
import com.tang.account.databinding.LayoutImportExportBinding
import com.tang.account.ui.RecordRecyclerAdapter
import com.tang.account.viewmodel.ExportExcelViewModel

/*导出至Excel的activity*/
class ExportToExcelActivity : MyAppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(ExportExcelViewModel::class.java)
    }
    private lateinit var binding: LayoutImportExportBinding
    private lateinit var adapter: RecordRecyclerAdapter

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutImportExportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val layoutManager = LinearLayoutManager(this)
        binding.recordRecycler.layoutManager = layoutManager
        adapter =
            RecordRecyclerAdapter(this, viewModel)
        binding.recordRecycler.adapter = adapter // recycler设置
        binding.checkboxToolBar.cancelButton.setOnClickListener { finish() }
        binding.checkboxToolBar.selectAllButton.setOnClickListener { // 全选按钮监听
            if (viewModel.isCheckedAll.value == true) {
                binding.checkboxToolBar.selectAllButton.setText(R.string.select_all)
                viewModel.isCheckedAll.value = false
            } else {
                binding.checkboxToolBar.selectAllButton.setText(R.string.select_none)
                viewModel.isCheckedAll.value = true
            }
        }
        viewModel.onCheckedChange.observe(this, { // 每次进行选择后更新已选中数目值
            binding.checkboxToolBar.selectedCountText.text =
                "已选中" + viewModel.getCheckedCount() + "项"
        })
        viewModel.isCheckedAll.observe(this, { flag -> // 全选状态监听
            if (flag) { // 全选时将所有record选中
                for (r in viewModel.getList()) {
                    viewModel.addChecked(r.id)
                }
            } else {
                viewModel.clearChecked()
            }
        })
        viewModel.loadListFromDB.observe(this, { flag -> // 加载record
            if (!flag) { // 加载失败
                binding.progressBar.visibility = View.INVISIBLE
                binding.fileErrorText.visibility = View.VISIBLE
            } else { // 进行显示并更改按钮监听和文字
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.INVISIBLE
                binding.recordRecycler.visibility = View.VISIBLE
                binding.confirmButton.setText(R.string.finish_export)
                binding.confirmButton.setOnClickListener {
                    viewModel.exportRecords()
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recordRecycler.visibility = View.INVISIBLE
                    binding.confirmButton.setOnClickListener {}
                }
            }
        })
        viewModel.isFinishExport.observe(this, { t -> // 导出完成后操作
            if (t) {
                AccountApplication.showToast("已导出至Download目录")
                finish()
            }
        })
        viewModel.loadRecords() // 启动activity时进行加载
    }
}