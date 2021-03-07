package com.tang.account.ui.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.tang.account.MyAppCompatActivity
import com.tang.account.databinding.ActivityDetailBinding
import com.tang.account.logic.util.RecordTransformer
import com.tang.account.logic.util.TimeUtil
import com.tang.account.model.Record
import com.tang.account.ui.mybasewidget.TipDialogFragment
import com.tang.account.viewmodel.DetailViewModel

/*record内容显示activity*/
class DetailActivity : MyAppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(DetailViewModel::class.java) }

    /*方式选择弹窗*/
    private lateinit var wayDialogFragment: WayDialogFragment

    /*类别选择弹窗*/
    private lateinit var categoryDialogFragment: CategoryDialogFragment

    /*时间日期编辑弹窗*/
    private lateinit var timeDialogFragment: TimeDialogFragment

    /*界面布局*/
    private lateinit var binding: ActivityDetailBinding

    companion object {
        /*删除操作的标志量*/
        const val OPERATION_DELETE = 1

        /*保存操作的标志量*/
        const val OPERATION_SAVE = 2

        /**
         * 外部启动接口
         *
         * @param context 父环境
         * @param record 被选中的record
         * @param requestCode 从外部不同控件启动的标志变量
         */
        fun actionStart(context: AppCompatActivity, record: Record, requestCode: Int) {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("record_data", record)
            context.startActivityForResult(intent, requestCode)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val record = intent.getSerializableExtra("record_data") as Record
        viewModel.setData(record) // 获取传入的record并设置参数显示
        if (record.id != Record.DEFAULT_ID) { // 为新增操作时隐藏删除按钮
            binding.deleteButton.visibility = View.VISIBLE
        }
        wayDialogFragment = WayDialogFragment(viewModel)
        categoryDialogFragment = CategoryDialogFragment(viewModel)
        timeDialogFragment = TimeDialogFragment(viewModel) // 初始化弹窗
        binding.amountText.addTextChangedListener(object : TextWatcher { // 金额输入框文字更改监听
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //如果第一个数字为0，第二个不为点，就不允许输入
                if (s.toString().startsWith("0") && s.toString().trim().length > 1) {
                    if (s.toString().substring(1, 2) != ".") {
                        binding.amountText.setText(s?.subSequence(0, 1))
                        binding.amountText.setSelection(1)
                        return
                    }
                }
                //如果第一为点，直接显示0.
                if (s.toString().startsWith(".")) {
                    binding.amountText.setText("0.")
                    binding.amountText.setSelection(2)
                    return
                }
                //限制输入小数位数(2位)
                if (s.toString().contains(".")) {
                    val length = s.toString().length
                    if (s.toString().indexOf(".") + 2 < length - 1) {
                        val str = s.toString().subSequence(0, s.toString().indexOf(".") + 3)
                        binding.amountText.setText(str)
                        binding.amountText.setSelection(str.length)
                    }
                }
            }
        })
        binding.backButton.setOnClickListener { finish() }
        binding.deleteButton.setOnClickListener {
            TipDialogFragment.show(this, "确认删除本条记录?") {
                viewModel.delete()
                val intent = Intent()
                intent.putExtra("operation", OPERATION_DELETE)
                setResult(Activity.RESULT_OK, intent) // 将删除操作标志量传回
                finish()
            }
        }
        binding.saveButton.setOnClickListener { // 保存
            if (binding.amountText.text.toString().isNotEmpty()) {
                viewModel.setAmount(binding.amountText.text.toString().toFloat())
                viewModel.setInfo(binding.infoText.text.toString())
                viewModel.save()
            }
        }
        viewModel.isSaved.observe(this, { // 保存完成后结束活动
            val intent = Intent()
            intent.putExtra("operation", OPERATION_SAVE)
            intent.putExtra("data", viewModel.getData())
            setResult(Activity.RESULT_OK, intent) // 将保存操作标志量和修改后的record传回
            finish()
        })
        binding.categoryButton.setOnClickListener {
            categoryDialogFragment.show(supportFragmentManager, "category_dialog")
        }
        binding.timeButton.setOnClickListener {
            timeDialogFragment.show(supportFragmentManager, "time_dialog")
        }
        binding.wayButton.setOnClickListener {
            wayDialogFragment.show(supportFragmentManager, "way_dialog")
        }
        // 各个输入和选择控件的监听
        viewModel.amount.observe(this, { t ->
            if (t != Record.DEFAULT_ILLEGAL_AMOUNT) {
                binding.amountText.setText(t.toString())
            }
        })
        viewModel.category.observe(this, { t ->
            binding.categoryImage.setImageResource(RecordTransformer.getImageId(t))
            binding.categoryText.text = RecordTransformer.transform(t)
        })
        viewModel.time.observe(this, { t ->
            binding.timeText.text = TimeUtil.stampToMonth(t)
                .toString() + "月" + TimeUtil.stampToDate(t) + "日 " + TimeUtil.stampToTime(t)
        })
        viewModel.way.observe(this, { t ->
            binding.wayText.text = RecordTransformer.transform(t)
        })
        viewModel.info.observe(this, { t ->
            if (t.isNotEmpty()) {
                binding.infoText.setText(t)
            }
        })
    }
}