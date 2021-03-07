package com.tang.account.ui.statistics.categorydetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.tang.account.AccountApplication
import com.tang.account.MyAppCompatActivity
import com.tang.account.R
import com.tang.account.databinding.ActivityCategoryDetailBinding
import com.tang.account.ui.RecordRecyclerAdapter
import com.tang.account.ui.detail.DetailActivity
import com.tang.account.viewmodel.CategoryDetailViewModel

/*某段时间内某类别详细activity*/
class CategoryDetailActivity : MyAppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(CategoryDetailViewModel::class.java) }
    private lateinit var binding:ActivityCategoryDetailBinding
    private lateinit var adapter: RecordRecyclerAdapter

    companion object {

        /*数据被修改状态量*/
        const val OPERATION_DATA_FIXED = 1

        /**
         * 外部启动接口
         *
         * @param context 父环境
         * @param category 类别
         * @param start 起时间
         * @param end 止时间
         * @param title 标题
         * @param requestCode 从外部不同控件启动的标志变量
         */
        fun actionStart(
            context: AppCompatActivity,
            category: String,
            start: Long,
            end: Long,
            title: String,
            requestCode: Int
        ) {
            val intent = Intent(context, CategoryDetailActivity::class.java)
            intent.putExtra("category", category)
            intent.putExtra("start", start)
            intent.putExtra("end", end)
            intent.putExtra("title", title)
            context.startActivityForResult(intent, requestCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val layoutManager = LinearLayoutManager(this)
        binding.detailRecycler.layoutManager = layoutManager
        adapter = RecordRecyclerAdapter(this, viewModel)
        binding.detailRecycler.adapter = adapter // 初始化recycler
        binding.backButton.setOnClickListener { onBackPressed() } // 返回按钮监听
        binding.titleText.text = intent.getStringExtra("title") // 标题文字
        binding.orderText.setOnClickListener { // 排序按钮点击监听
            if (viewModel.orderStatus == viewModel.ORDER_TIME) { // 根据排序标志量进行排序
                viewModel.orderStatus = viewModel.ORDER_AMOUNT
                binding.orderText.setText(R.string.orderByAmount)
                viewModel.setOrderByAmount()
            } else {
                viewModel.orderStatus = viewModel.ORDER_TIME
                binding.orderText.setText(R.string.orderByTime)
                viewModel.setOrderByTime()
            }
        }
        viewModel.sortFinished.observe(this, { // 排序完成后进行数据显示
            adapter.notifyDataSetChanged()
        })
        viewModel.isCheckedStatus.observe(this, { flag -> // 监听多选状态,保持多选状态关闭
            if (flag) viewModel.isCheckedStatus.value = false
        })
        viewModel.loadRecords( // 创建activity时加载数据
            intent.getLongExtra("start", 0),
            intent.getLongExtra("end", 0),
            intent.getStringExtra("category")!!
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AccountApplication.RESULT_DETAIL && resultCode == Activity.RESULT_OK) {
            when (data?.getIntExtra("operation", -1)) {
                DetailActivity.OPERATION_SAVE -> { // 保存操作
                    viewModel.reloadRecords()
                }
                DetailActivity.OPERATION_DELETE -> { // 删除操作
                    viewModel.reloadRecords()
                }
                // 数据操作后均重新加载record
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("operation", viewModel.dataFixedStatus)
        setResult(Activity.RESULT_OK, intent) // 将数据修改操作状态量传回
        finish()
    }
}