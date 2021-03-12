package com.tang.account.ui.detail.preference

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.tang.account.MyAppCompatActivity
import com.tang.account.databinding.ActivityDetailPreferenceBinding
import com.tang.account.logic.util.RecordTransformer
import com.tang.account.ui.detail.CategoryDialogFragment
import com.tang.account.ui.detail.WayDialogFragment
import com.tang.account.viewmodel.DetailPreferenceViewModel

class DetailPreferenceActivity : MyAppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(DetailPreferenceViewModel::class.java) }

    /*方式选择弹窗*/
    private lateinit var wayDialogFragment: WayDialogFragment

    /*类别选择弹窗*/
    private lateinit var categoryDialogFragment: CategoryDialogFragment

    /*界面布局*/
    private lateinit var binding: ActivityDetailPreferenceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPreferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        wayDialogFragment = WayDialogFragment(viewModel)
        categoryDialogFragment = CategoryDialogFragment(viewModel) // 初始化弹窗
        binding.backButton.setOnClickListener {
            finish()
        }
        viewModel.loadPrefer()
        viewModel.categoryPrefer.observe(this, {
            binding.categoryText.text = RecordTransformer.transform(it)
        })
        viewModel.wayPrefer.observe(this, {
            binding.wayText.text = RecordTransformer.transform(it)
        })
        binding.categorySelector.setOnClickListener {
            categoryDialogFragment.show(supportFragmentManager, "category_dialog")
        }
        binding.waySelector.setOnClickListener {
            wayDialogFragment.show(supportFragmentManager, "way_dialog")
        }
    }
}