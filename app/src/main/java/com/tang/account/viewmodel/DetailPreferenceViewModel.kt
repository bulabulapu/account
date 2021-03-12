package com.tang.account.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.account.logic.Repository
import com.tang.account.logic.util.RecordTransformer
import kotlinx.coroutines.launch

/*DetailPreferenceActivity的ViewModel*/
class DetailPreferenceViewModel : ViewModel() {

    /*当前首选项设置*/
    val categoryPrefer = MutableLiveData(RecordTransformer.CAN_YIN)
    val wayPrefer = MutableLiveData(RecordTransformer.ZHI_FU_BAO)

    /**
     * 更改首选类别
     * @param category 设置的类别
     */
    fun setCategory(category: String) {
        categoryPrefer.value = category
        savePrefer()
    }

    /**
     * 更改首选方式
     * @param way 设置的方式
     */
    fun setWay(way: String) {
        wayPrefer.value = way
        savePrefer()
    }

    /**
     * 保存首选设置到本地
     */
    private fun savePrefer() {
        viewModelScope.launch {
            categoryPrefer.value?.let { category ->
                wayPrefer.value?.let { way ->
                    Repository.saveDetailPreference(
                        category,
                        way
                    )
                }
            }
        }
    }

    /**
     * 从本地加载首选设置
     */
    fun loadPrefer() {
        viewModelScope.launch {
            val currentPrefer = Repository.loadDetailPreference()
            categoryPrefer.value = currentPrefer["category_preference"]
            wayPrefer.value = currentPrefer["way_preference"]
        }
    }
}