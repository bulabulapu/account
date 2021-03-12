package com.tang.account.ui.importfrommibak

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.tang.account.AccountApplication
import com.tang.account.MyAppCompatActivity
import com.tang.account.R
import com.tang.account.databinding.LayoutImportExportBinding
import com.tang.account.ui.RecordRecyclerAdapter
import com.tang.account.viewmodel.ImportMiBakViewModel

class ImportFromMiBakActivity : MyAppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(ImportMiBakViewModel::class.java)
    }
    private lateinit var binding: LayoutImportExportBinding
    private lateinit var adapter: RecordRecyclerAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutImportExportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        this.startActivityForResult(
            intent,
            AccountApplication.REQUEST_GET_MI_BAK
        )// 启动选择备份文件activity
        val layoutManager = LinearLayoutManager(this)
        binding.recordRecycler.layoutManager = layoutManager
        adapter =
            RecordRecyclerAdapter(this, viewModel)
        binding.recordRecycler.adapter = adapter // recycler设置
        binding.fileErrorText.setText(R.string.mi_error)
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
        binding.confirmButton.setOnClickListener { // 确认按钮监听
            this.startActivityForResult(intent, AccountApplication.REQUEST_GET_MI_BAK)
        }
        viewModel.loadListFromMiBakStatus.observe(this, { flag -> // 加载record
            if (!flag) { // 加载失败
                binding.progressBar.visibility = View.INVISIBLE
                binding.fileErrorText.visibility = View.VISIBLE
            } else { // 进行显示并更改按钮监听和文字
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.INVISIBLE
                binding.recordRecycler.visibility = View.VISIBLE
                binding.confirmButton.setText(R.string.finish_import)
                binding.confirmButton.setOnClickListener {
                    viewModel.saveCheckedRecordsToDB()
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recordRecycler.visibility = View.INVISIBLE
                    binding.confirmButton.setOnClickListener {}
                }
            }
        })
        viewModel.onCheckedChange.observe(this, { // 每次进行选择后更新已选中数目值
            binding.checkboxToolBar.selectedCountText.text = "已选中" + viewModel.getCheckedCount() + "项"
        })
        viewModel.isFinishSave.observe(this, { flag -> // 导入完成后操作
            if (flag) {
                val intent1 = Intent()
                intent1.putExtra("status", true)
                setResult(Activity.RESULT_OK, intent1)
                finish()
            }
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AccountApplication.REQUEST_GET_MI_BAK -> if (resultCode == Activity.RESULT_OK && data != null) {
                val uri = data.data
                uri?.let { // 获取文件uri后加载数据
                    binding.progressBar.visibility = View.VISIBLE
                    binding.fileErrorText.visibility = View.INVISIBLE
                    viewModel.loadRecordsFromMiBak(it)
                }
            }
        }
    }
}