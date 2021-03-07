package com.tang.account

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/*自定义AppCompatActivity*/
open class MyAppCompatActivity : AppCompatActivity() {
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) { // 设置状态栏字体颜色
        super.onCreate(savedInstanceState)
        val decorView = window.decorView
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) { // 为深色模式时,设置状态栏字体颜色
            decorView.systemUiVisibility =
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decorView.systemUiVisibility =
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        this.window.setBackgroundDrawable(getDrawable(R.color.defaultBgColor)) // 设置背景
    }
}