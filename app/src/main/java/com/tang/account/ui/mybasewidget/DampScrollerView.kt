package com.tang.account.ui.mybasewidget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.TranslateAnimation
import android.widget.ScrollView
import kotlin.math.abs

/**
 * 带回弹效果的ScrollView
 */
class DampScrollerView : ScrollView {

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context) : super(context)

    init {
        this.isVerticalScrollBarEnabled = false
    }

    private var previousY = 0 // 滑动过程中前一次记录位置
    private var startY = 0 // 触摸初始位置
    private var currentY = 0 // 当前触摸位置
    private var distanceY = 0 // 当前触摸位置与前一次记录位置距离
    private lateinit var childView: View // 子view
    private val childViewFirstPos = Rect() // 子view的初始位置
    private var moveHeight = 0f // 子view移动的距离

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 0) { //初始化子view
            childView = getChildAt(0)
        }
    }

    // 主界面快速下滑再上滑会导致子view上移 todo
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = ev.y.toInt()
                    previousY = startY // 记录触摸点初始位置及前一次记录
                    childViewFirstPos.set(
                        childView.left,
                        childView.top,
                        childView.right,
                        childView.bottom
                    ) // 记录子view初始位置
                    moveHeight = 0f
                }
                MotionEvent.ACTION_MOVE -> {
                    currentY = ev.y.toInt()
                    distanceY = currentY - previousY // 两次记录之间移动距离
                    previousY = currentY
                    if ((!childView.canScrollVertically(-1) && currentY > startY) ||
                        (!childView.canScrollVertically(1) && currentY < startY) ||
                        abs(moveHeight) > 10f
                    ) { // 判断是否滑到顶或底
                        var div = abs(moveHeight / height) - 0.6f
                        div = if (div > 0) {
                            0f
                        } else {
                            div
                        }
                        val damping = div * div // 根据子view的移动距离和控件高度确定阻尼,阻尼变化公式为(x-0.6)^2,x为比值
                        if (moveHeight * distanceY >= 0) { // 滑动方向是否与控件移动方向相同
                            moveHeight += distanceY * damping
                        } else {
                            moveHeight += distanceY
                        }
                        childView.layout(
                            childViewFirstPos.left,
                            (childViewFirstPos.top + moveHeight).toInt(),
                            childViewFirstPos.right,
                            (childViewFirstPos.bottom + moveHeight).toInt()
                        )
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (!childViewFirstPos.isEmpty) {
                        recovery()
                        childView.layout(
                            childViewFirstPos.left,
                            childViewFirstPos.top,
                            childViewFirstPos.right,
                            childViewFirstPos.bottom
                        )
                    }
                    startY = 0
                    currentY = 0
                    childViewFirstPos.setEmpty()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 恢复子view的位置
     */
    private fun recovery() {
        val animation =
            TranslateAnimation(0f, 0f, childView.top.toFloat(), childViewFirstPos.top.toFloat())
        animation.duration = 400
        animation.fillAfter = true
        animation.interpolator = Interpolator { input ->
            1 - (1 - input) * (1 - input) * (1 - input) * (1 - input) * (1 - input)
        }
        childView.animation = animation
    }
}